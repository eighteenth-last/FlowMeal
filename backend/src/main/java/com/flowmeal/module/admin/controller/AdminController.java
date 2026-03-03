package com.flowmeal.module.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flowmeal.common.result.Result;
import com.flowmeal.module.coupon.entity.Coupon;
import com.flowmeal.module.coupon.mapper.CouponMapper;
import com.flowmeal.module.merchant.entity.Merchant;
import com.flowmeal.module.merchant.mapper.MerchantMapper;
import com.flowmeal.module.order.entity.Orders;
import com.flowmeal.module.order.mapper.OrdersMapper;
import com.flowmeal.module.material.entity.MaterialReport;
import com.flowmeal.module.material.mapper.MaterialReportMapper;
import com.flowmeal.module.procurement.entity.ProcurementOrder;
import com.flowmeal.module.procurement.mapper.ProcurementOrderMapper;
import com.flowmeal.module.supplier.entity.Supplier;
import com.flowmeal.module.supplier.mapper.SupplierMapper;
import com.flowmeal.module.rider.entity.Rider;
import com.flowmeal.module.rider.mapper.RiderMapper;
import com.flowmeal.module.user.entity.User;
import com.flowmeal.module.user.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "管理员接口")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserMapper userMapper;
    private final MerchantMapper merchantMapper;
    private final RiderMapper riderMapper;
    private final OrdersMapper ordersMapper;
    private final CouponMapper couponMapper;
    private final ProcurementOrderMapper procurementMapper;
    private final SupplierMapper supplierMapper;
    private final MaterialReportMapper materialReportMapper;

    @Operation(summary = "原料消耗上报列表")
    @GetMapping("/materials")
    public Result<List<MaterialReport>> listMaterials(
            @RequestParam(required = false) Long merchantId) {
        LambdaQueryWrapper<MaterialReport> qw = new LambdaQueryWrapper<MaterialReport>()
                .orderByDesc(MaterialReport::getReportTime);
        if (merchantId != null) qw.eq(MaterialReport::getMerchantId, merchantId);
        return Result.success(materialReportMapper.selectList(qw));
    }

    @Operation(summary = "原料消耗聚合汇总（按原料名称分组）")
    @GetMapping("/materials/summary")
    public Result<List<Map<String, Object>>> materialsSummary() {
        List<MaterialReport> all = materialReportMapper.selectList(
                new LambdaQueryWrapper<MaterialReport>().orderByDesc(MaterialReport::getReportTime));
        // 预加载所有涉及的商家
        Set<Long> mids = all.stream().map(MaterialReport::getMerchantId).collect(Collectors.toSet());
        Map<Long, String> merchantNameMap = new HashMap<>();
        if (!mids.isEmpty()) {
            merchantMapper.selectList(new LambdaQueryWrapper<Merchant>().in(Merchant::getId, mids))
                    .forEach(m -> merchantNameMap.put(m.getId(), m.getShopName()));
        }
        Map<String, List<MaterialReport>> grouped = all.stream()
                .collect(Collectors.groupingBy(MaterialReport::getMaterialName));
        List<Map<String, Object>> summary = grouped.entrySet().stream()
                .map(e -> {
                    List<MaterialReport> list = e.getValue();
                    long total = list.stream().mapToLong(r -> r.getConsumed()).sum();
                    List<Long> merchantIds = list.stream().map(MaterialReport::getMerchantId).distinct().collect(Collectors.toList());
                    List<String> merchantNames = merchantIds.stream()
                            .map(id -> merchantNameMap.getOrDefault(id, "商家" + id))
                            .collect(Collectors.toList());
                    String unit = list.get(0).getUnit();
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("materialName", e.getKey());
                    m.put("totalConsumed", total);
                    m.put("unit", unit);
                    m.put("merchantCount", (long) merchantIds.size());
                    m.put("recordCount", (long) list.size());
                    m.put("merchantIds", merchantIds);
                    m.put("merchantNames", merchantNames);
                    return m;
                })
                .sorted((a, b) -> Long.compare((long) b.get("totalConsumed"), (long) a.get("totalConsumed")))
                .collect(Collectors.toList());
        return Result.success(summary);
    }

    @Operation(summary = "原料消耗明细列表（含商家名）")
    @GetMapping("/materials/detail")
    public Result<List<Map<String, Object>>> listMaterialsDetail(
            @RequestParam(required = false) Long merchantId) {
        LambdaQueryWrapper<MaterialReport> qw = new LambdaQueryWrapper<MaterialReport>()
                .orderByDesc(MaterialReport::getReportTime);
        if (merchantId != null) qw.eq(MaterialReport::getMerchantId, merchantId);
        List<MaterialReport> list = materialReportMapper.selectList(qw);
        Set<Long> mids = list.stream().map(MaterialReport::getMerchantId).collect(Collectors.toSet());
        Map<Long, String> merchantNameMap = new HashMap<>();
        if (!mids.isEmpty()) {
            merchantMapper.selectList(new LambdaQueryWrapper<Merchant>().in(Merchant::getId, mids))
                    .forEach(m -> merchantNameMap.put(m.getId(), m.getShopName()));
        }
        List<Map<String, Object>> result = list.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("merchantId", r.getMerchantId());
            m.put("shopName", merchantNameMap.getOrDefault(r.getMerchantId(), "商家" + r.getMerchantId()));
            m.put("materialName", r.getMaterialName());
            m.put("consumed", r.getConsumed());
            m.put("unit", r.getUnit());
            m.put("reportTime", r.getReportTime());
            m.put("createdAt", r.getCreatedAt());
            return m;
        }).collect(Collectors.toList());
        return Result.success(result);
    }

    @Operation(summary = "数据大盘汇总")
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        long userCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getStatus, 1));
        long merchantCount = merchantMapper.selectCount(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getAuditStatus, 1).eq(Merchant::getStatus, 1));
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        List<Orders> todayOrders = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .ge(Orders::getCreatedAt, todayStart).lt(Orders::getCreatedAt, todayEnd));
        long orderCount = todayOrders.size();
        BigDecimal revenue = todayOrders.stream()
                .filter(o -> "COMPLETED".equals(o.getStatus()))
                .map(Orders::getActualAmount).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userCount", userCount);
        data.put("merchantCount", merchantCount);
        data.put("orderCount", orderCount);
        data.put("revenue", revenue);
        return Result.success(data);
    }

    @Operation(summary = "近7天订单趋势")
    @GetMapping("/dashboard/order-trend")
    public Result<List<Map<String, Object>>> orderTrend() {
        return Result.success(buildDayTrend(7, false));
    }

    @Operation(summary = "近7天营业额趋势")
    @GetMapping("/dashboard/revenue-trend")
    public Result<List<Map<String, Object>>> revenueTrend() {
        return Result.success(buildDayTrend(7, true));
    }

    @Operation(summary = "用户列表")
    @GetMapping("/users")
    public Result<Page<User>> listUsers(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size,
                                         @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>().orderByDesc(User::getCreatedAt);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(User::getUsername, keyword).or().like(User::getPhone, keyword));
        }
        return Result.success(userMapper.selectPage(new Page<>(page, size), wrapper));
    }

    @Operation(summary = "冻结/解冻用户")
    @PutMapping("/users/{id}/status")
    public Result<Void> toggleUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getId, id).set(User::getStatus, status));
        return Result.success();
    }

    @Operation(summary = "商家列表")
    @GetMapping("/merchants")
    public Result<Page<Merchant>> listMerchants(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<Merchant>()
                .orderByDesc(Merchant::getCreatedAt);
        if (auditStatus != null) {
            wrapper.eq(Merchant::getAuditStatus, auditStatus);
        } else if (status != null && !status.isBlank()) {
            Map<String, Integer> sm = Map.of("PENDING", 0, "APPROVED", 1, "REJECTED", 2);
            Integer mapped = sm.get(status.toUpperCase());
            if (mapped != null) wrapper.eq(Merchant::getAuditStatus, mapped);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Merchant::getShopName, keyword)
                    .or().like(Merchant::getPhone, keyword));
        }
        return Result.success(merchantMapper.selectPage(new Page<>(page, size), wrapper));
    }

    @Operation(summary = "审核商家")
    @PutMapping("/merchants/{id}/audit")
    public Result<Void> auditMerchant(@PathVariable Long id,
                                       @RequestParam Integer auditStatus,
                                       @RequestParam(required = false) String remark) {
        merchantMapper.update(null, new LambdaUpdateWrapper<Merchant>()
                .eq(Merchant::getId, id).set(Merchant::getAuditStatus, auditStatus)
                .set(Merchant::getAuditRemark, remark));
        return Result.success();
    }

    @Operation(summary = "禁用/启用商家")
    @PutMapping("/merchants/{id}/status")
    public Result<Void> toggleMerchantStatus(@PathVariable Long id, @RequestParam Integer status) {
        merchantMapper.update(null, new LambdaUpdateWrapper<Merchant>()
                .eq(Merchant::getId, id).set(Merchant::getStatus, status));
        return Result.success();
    }

    @Operation(summary = "外卖员列表")
    @GetMapping("/riders")
    public Result<Page<Rider>> listRiders(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(required = false) Integer auditStatus,
                                           @RequestParam(required = false) Integer onlineStatus,
                                           @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Rider> wrapper = new LambdaQueryWrapper<Rider>()
                .orderByDesc(Rider::getCreatedAt);
        if (auditStatus != null) wrapper.eq(Rider::getAuditStatus, auditStatus);
        if (onlineStatus != null) wrapper.eq(Rider::getOnlineStatus, onlineStatus);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Rider::getRealName, keyword).or().like(Rider::getPhone, keyword));
        }
        return Result.success(riderMapper.selectPage(new Page<>(page, size), wrapper));
    }

    @Operation(summary = "审核外卖员")
    @PutMapping("/riders/{id}/audit")
    public Result<Void> auditRider(@PathVariable Long id, @RequestParam Integer auditStatus) {
        riderMapper.update(null, new LambdaUpdateWrapper<Rider>()
                .eq(Rider::getId, id).set(Rider::getAuditStatus, auditStatus));
        return Result.success();
    }

    @Operation(summary = "封禁/解封外卖员")
    @PutMapping("/riders/{id}/status")
    public Result<Void> toggleRiderStatus(@PathVariable Long id, @RequestParam Integer status) {
        riderMapper.update(null, new LambdaUpdateWrapper<Rider>()
                .eq(Rider::getId, id).set(Rider::getStatus, status));
        return Result.success();
    }

    @Operation(summary = "管理员强制设置骑手在线/离线")
    @PutMapping("/riders/{id}/online")
    public Result<Void> setRiderOnline(@PathVariable Long id, @RequestParam Integer onlineStatus) {
        riderMapper.update(null, new LambdaUpdateWrapper<Rider>()
                .eq(Rider::getId, id).set(Rider::getOnlineStatus, onlineStatus));
        return Result.success();
    }

    @Operation(summary = "获取骑手当前进行中的订单")
    @GetMapping("/riders/{id}/active-orders")
    public Result<List<Map<String, Object>>> riderActiveOrders(@PathVariable Long id) {
        List<Orders> orders = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getRiderId, id)
                .in(Orders::getStatus, "RIDER_ACCEPTED", "DELIVERING")
                .orderByAsc(Orders::getCreatedAt));
        List<Map<String, Object>> result = orders.stream().map(o -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", o.getId());
            m.put("orderNo", o.getOrderNo());
            m.put("status", o.getStatus());
            m.put("actualAmount", o.getActualAmount());
            m.put("addressSnapshot", o.getAddressSnapshot());
            m.put("createdAt", o.getCreatedAt());
            return m;
        }).collect(Collectors.toList());
        return Result.success(result);
    }

    @Operation(summary = "将订单转派给其他骑手")
    @PutMapping("/orders/{orderId}/reassign")
    public Result<Void> reassignOrder(@PathVariable Long orderId, @RequestParam Long toRiderId) {
        Rider target = riderMapper.selectById(toRiderId);
        if (target == null || target.getAuditStatus() != 1 || target.getOnlineStatus() != 1) {
            return Result.fail("目标骑手不存在或不在线");
        }
        ordersMapper.update(null, new LambdaUpdateWrapper<Orders>()
                .eq(Orders::getId, orderId)
                .in(Orders::getStatus, "RIDER_ACCEPTED", "DELIVERING")
                .set(Orders::getRiderId, toRiderId));
        return Result.success();
    }

    @Operation(summary = "全平台订单列表")
    @GetMapping("/orders")
    public Result<Page<Orders>> listOrders(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<Orders>()
                .orderByDesc(Orders::getCreatedAt);
        if (status != null && !status.isBlank()) wrapper.eq(Orders::getStatus, status);
        return Result.success(ordersMapper.selectPage(new Page<>(page, size), wrapper));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/orders/{id}")
    public Result<Orders> orderDetail(@PathVariable Long id) {
        return Result.success(ordersMapper.selectById(id));
    }

    @Operation(summary = "查询优惠券列表")
    @GetMapping("/coupons")
    public Result<List<Coupon>> listCoupons() {
        List<Coupon> list = couponMapper.selectList(
                new LambdaQueryWrapper<Coupon>().orderByDesc(Coupon::getCreatedAt));
        return Result.success(list);
    }

    @Operation(summary = "创建优惠券")
    @PostMapping("/coupons")
    public Result<Void> createCoupon(@RequestBody Coupon coupon) {
        coupon.setClaimedCount(0);
        coupon.setStatus("ACTIVE");
        couponMapper.insert(coupon);
        return Result.success();
    }

    @Operation(summary = "停用优惠券")
    @PutMapping("/coupons/{id}/disable")
    public Result<Void> disableCoupon(@PathVariable Long id) {
        couponMapper.update(null, new LambdaUpdateWrapper<Coupon>()
                .eq(Coupon::getId, id).set(Coupon::getStatus, "INACTIVE"));
        return Result.success();
    }

    @Operation(summary = "删除优惠券")
    @DeleteMapping("/coupons/{id}")
    public Result<Void> deleteCoupon(@PathVariable Long id) {
        couponMapper.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "采购单列表")
    @GetMapping("/procurement")
    public Result<List<ProcurementOrder>> listProcurement() {
        List<ProcurementOrder> list = procurementMapper.selectList(
                new LambdaQueryWrapper<ProcurementOrder>().orderByDesc(ProcurementOrder::getCreatedAt));
        return Result.success(list);
    }

    @Operation(summary = "新建采购单")
    @PostMapping("/procurement")
    public Result<Void> createProcurement(@RequestBody ProcurementOrder order) {
        order.setStatus("PENDING");
        procurementMapper.insert(order);
        return Result.success();
    }

    @Operation(summary = "更新采购单状态")
    @PutMapping("/procurement/{id}/status")
    public Result<Void> updateProcurementStatus(@PathVariable Long id, @RequestParam String status) {
        procurementMapper.update(null, new LambdaUpdateWrapper<ProcurementOrder>()
                .eq(ProcurementOrder::getId, id).set(ProcurementOrder::getStatus, status));
        return Result.success();
    }

    // ───────────── 供应商管理 ─────────────

    @Operation(summary = "供应商列表")
    @GetMapping("/suppliers")
    public Result<List<Supplier>> listSuppliers() {
        return Result.success(supplierMapper.selectList(
                new LambdaQueryWrapper<Supplier>().orderByAsc(Supplier::getCreatedAt)));
    }

    @Operation(summary = "新增供应商")
    @PostMapping("/suppliers")
    public Result<Void> createSupplier(@RequestBody Supplier supplier) {
        supplier.setStatus(1);
        supplierMapper.insert(supplier);
        return Result.success();
    }

    @Operation(summary = "编辑供应商")
    @PutMapping("/suppliers/{id}")
    public Result<Void> updateSupplier(@PathVariable Long id, @RequestBody Supplier supplier) {
        supplier.setId(id);
        supplierMapper.updateById(supplier);
        return Result.success();
    }

    @Operation(summary = "切换供应商状态")
    @PutMapping("/suppliers/{id}/status")
    public Result<Void> toggleSupplierStatus(@PathVariable Long id, @RequestParam Integer status) {
        supplierMapper.update(null, new LambdaUpdateWrapper<Supplier>()
                .eq(Supplier::getId, id).set(Supplier::getStatus, status));
        return Result.success();
    }

    @Operation(summary = "删除供应商")
    @DeleteMapping("/suppliers/{id}")
    public Result<Void> deleteSupplier(@PathVariable Long id) {
        supplierMapper.deleteById(id);
        return Result.success();
    }

    private List<Map<String, Object>> buildDayTrend(int days, boolean revenueMode) {
        LocalDate today = LocalDate.now();
        LocalDateTime rangeStart = today.minusDays(days - 1).atStartOfDay();
        List<Orders> orders = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .ge(Orders::getCreatedAt, rangeStart));
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            List<Orders> dayOrders = orders.stream()
                    .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().toLocalDate().equals(date))
                    .collect(Collectors.toList());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", date.format(fmt));
            if (revenueMode) {
                BigDecimal rev = dayOrders.stream()
                        .filter(o -> "COMPLETED".equals(o.getStatus()))
                        .map(Orders::getActualAmount).filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                item.put("revenue", rev);
            } else {
                item.put("count", (long) dayOrders.size());
            }
            result.add(item);
        }
        return result;
    }
}
