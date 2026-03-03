package com.flowmeal.module.rider.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flowmeal.common.result.Result;
import com.flowmeal.module.rider.entity.DeliveryRecord;
import com.flowmeal.module.rider.entity.Rider;
import com.flowmeal.module.rider.mapper.DeliveryRecordMapper;
import com.flowmeal.module.rider.mapper.RiderMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 外卖员接口
 */
@Tag(name = "外卖员接口")
@RestController
@RequestMapping("/rider")
@RequiredArgsConstructor
public class RiderController {

    private final RiderMapper riderMapper;
    private final DeliveryRecordMapper deliveryRecordMapper;

    @Operation(summary = "获取个人信息")
    @GetMapping("/info")
    public Result<Rider> info() {
        Rider rider = riderMapper.selectById(getRiderId());
        if (rider != null) rider.setPassword(null);
        return Result.success(rider);
    }

    @Operation(summary = "修改基本信息（头像等）")
    @PutMapping("/info")
    public Result<Void> updateInfo(@RequestBody Rider req) {
        req.setId(getRiderId());
        req.setPassword(null);
        req.setAuditStatus(null);
        req.setStatus(null);
        riderMapper.updateById(req);
        return Result.success();
    }

    @Operation(summary = "切换在线/离线状态")
    @PutMapping("/online")
    public Result<Void> toggleOnline(@RequestBody Rider req) {
        riderMapper.update(null, new LambdaUpdateWrapper<Rider>()
                .eq(Rider::getId, getRiderId())
                .set(Rider::getOnlineStatus, req.getOnlineStatus()));
        return Result.success();
    }

    @Operation(summary = "历史配送记录（分页）")
    @GetMapping("/records")
    public Result<Page<DeliveryRecord>> records(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(deliveryRecordMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<DeliveryRecord>()
                        .eq(DeliveryRecord::getRiderId, getRiderId())
                        .orderByDesc(DeliveryRecord::getCreatedAt)));
    }

    private Long getRiderId() {
        return Long.parseLong(StpUtil.getLoginIdAsString().replace("rider:", ""));
    }
}
