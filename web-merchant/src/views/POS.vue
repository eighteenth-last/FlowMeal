<template>
  <div class="pos-wrap">
    <n-grid :cols="3" :x-gap="16" style="height:100%">
      <!-- ── 左：商品区 (占2列) ── -->
      <n-gi :span="2" style="display:flex;flex-direction:column;overflow:hidden">
        <n-card :bordered="false" style="border-radius:12px;flex:1;display:flex;flex-direction:column;overflow:hidden">
          <!-- 搜索 + 分类 -->
          <div style="margin-bottom:10px">
            <n-input
              v-model:value="searchKw"
              placeholder="搜索商品名称"
              clearable
              size="small"
              style="width:220px;margin-bottom:10px"
            >
              <template #prefix><n-icon><SearchOutline /></n-icon></template>
            </n-input>
            <n-tabs v-model:value="activeCategory" type="bar" size="small" @update:value="filterProducts">
              <n-tab name="all">全部</n-tab>
              <n-tab v-for="cat in categories" :key="cat.id" :name="String(cat.id)">{{ cat.name }}</n-tab>
            </n-tabs>
          </div>

          <!-- 商品网格 -->
          <div style="flex:1;overflow-y:auto">
            <n-empty v-if="!filteredProducts.length" description="暂无商品" style="margin-top:60px" />
            <n-grid v-else :cols="4" :x-gap="10" :y-gap="10">
              <n-gi v-for="p in filteredProducts" :key="p.id">
                <div class="product-card" @click="addToCart(p)">
                  <div class="product-img">
                    <img v-if="p.image" :src="p.image" :alt="p.name" />
                    <div v-else class="product-placeholder">🍱</div>
                  </div>
                  <div class="product-name">{{ p.name }}</div>
                  <div class="product-price">
                    ¥{{ p.discountPrice != null ? p.discountPrice : p.price }}
                    <span v-if="p.discountPrice != null" class="original-price">¥{{ p.price }}</span>
                  </div>
                </div>
              </n-gi>
            </n-grid>
          </div>
        </n-card>
      </n-gi>

      <!-- ── 右：收银台 ── -->
      <n-gi style="display:flex;flex-direction:column;overflow:hidden">
        <n-card :bordered="false" style="border-radius:12px;flex:1;display:flex;flex-direction:column;overflow:hidden">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span style="font-weight:700">🧾 收银台</span>
              <n-button text type="error" size="small" :disabled="!cart.length" @click="cart = []">清空</n-button>
            </div>
          </template>

          <!-- 购物车列表 -->
          <div style="flex:1;overflow-y:auto;margin:0 -4px;padding:0 4px">
            <n-empty v-if="!cart.length" description="点击商品加入" style="margin-top:40px" />
            <div v-for="item in cart" :key="item.id" class="cart-item">
              <div class="cart-name">
                <span>{{ item.name }}</span>
                <span style="color:#999;font-size:12px;margin-left:4px">¥{{ item.price }}</span>
              </div>
              <n-space align="center" :wrap="false" :size="4">
                <n-button circle size="tiny" @click="decItem(item)">-</n-button>
                <span style="min-width:22px;text-align:center;font-weight:600">{{ item.qty }}</span>
                <n-button circle size="tiny" type="primary" @click="item.qty++">+</n-button>
                <span style="color:#FFD100;font-weight:700;min-width:56px;text-align:right">
                  ¥{{ (item.price * item.qty).toFixed(2) }}
                </span>
              </n-space>
            </div>
          </div>

          <n-divider style="margin:10px 0" />

          <!-- 合计 & 结算 -->
          <div>
            <div style="display:flex;justify-content:space-between;align-items:baseline;margin-bottom:10px">
              <span style="color:#666;font-size:14px">共 {{ totalQty }} 件</span>
              <span style="font-size:24px;font-weight:900;color:#FFD100">¥{{ totalAmount }}</span>
            </div>
            <!-- 支付方式 -->
            <n-radio-group v-model:value="paymentType" style="margin-bottom:10px;display:flex;gap:8px">
              <n-radio-button value="CASH" style="flex:1;text-align:center">💵 现金</n-radio-button>
              <n-radio-button value="ALIPAY" style="flex:1;text-align:center">🔵 支付宝</n-radio-button>
              <n-radio-button value="WECHAT" style="flex:1;text-align:center">💚 微信</n-radio-button>
            </n-radio-group>
            <!-- 现金找零 -->
            <template v-if="paymentType === 'CASH'">
              <div style="display:flex;align-items:center;gap:8px;margin-bottom:10px">
                <span style="white-space:nowrap;font-size:13px;color:#666">收款</span>
                <n-input-number
                  v-model:value="cashReceived"
                  :min="0"
                  :step="1"
                  :precision="2"
                  style="flex:1"
                  placeholder="收款金额"
                />
                <span v-if="cashReceived > 0" style="white-space:nowrap;font-size:13px">
                  找零 <strong style="color:#f00">¥{{ changeAmount }}</strong>
                </span>
              </div>
            </template>
            <n-input v-model:value="remark" placeholder="备注（可选）" style="margin-bottom:10px" size="small" />
            <n-button
              type="primary"
              block
              size="large"
              :disabled="!cart.length"
              :loading="checkoutLoading"
              @click="doCheckout"
            >
              收 款 ¥{{ totalAmount }}
            </n-button>
          </div>
        </n-card>
      </n-gi>
    </n-grid>

    <!-- 收款成功小票弹窗 -->
    <n-modal v-model:show="showReceipt" preset="card" style="width:360px" :closable="false">
      <template #header>
        <div style="text-align:center;width:100%">✅ 收款成功</div>
      </template>
      <div class="receipt">
        <div class="receipt-amount">¥{{ lastOrder.totalAmount }}</div>
        <div class="receipt-no">订单号：{{ lastOrder.orderNo }}</div>
        <n-divider dashed />
        <div v-for="item in lastOrder.items" :key="item.id" class="receipt-row">
          <span>{{ item.name }} × {{ item.qty }}</span>
          <span>¥{{ (item.price * item.qty).toFixed(2) }}</span>
        </div>
        <n-divider dashed />
        <div class="receipt-row" style="font-weight:700">
          <span>合计</span>
          <span style="color:#FFD100">¥{{ lastOrder.totalAmount }}</span>
        </div>
        <div style="text-align:center;color:#999;font-size:12px;margin-top:8px">
          {{ paymentTypeLabel[lastOrder.paymentType] }} · {{ lastOrder.time }}
        </div>
      </div>
      <template #footer>
        <n-space justify="center">
          <n-button @click="printPosReceipt(lastOrder)">🗈️ 打印小票</n-button>
          <n-button type="primary" @click="showReceipt = false">完 成</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 扫码支付弹窗 -->
    <n-modal v-model:show="showQrModal" preset="card" style="width:340px" :closable="false" :mask-closable="false">
      <template #header>
        <div style="text-align:center;width:100%">
          {{ lastOrder.paymentType === 'ALIPAY' ? '支付宝' : '微信' }}扫码支付
        </div>
      </template>
      <div style="text-align:center;padding:8px 0">
        <p style="color:#666;font-size:13px;margin-bottom:12px">
          请使用{{ lastOrder.paymentType === 'ALIPAY' ? '支付宝' : '微信' }}扫描二维码付款
        </p>
        <img v-if="qrDataUrl" :src="qrDataUrl" style="width:200px;height:200px;border:1px solid #eee;border-radius:8px" />
        <div v-else style="width:200px;height:200px;display:flex;align-items:center;justify-content:center;background:#f5f5f5;border-radius:8px;margin:0 auto;color:#999">生成中…</div>
        <div style="margin-top:12px;font-size:32px;font-weight:900;color:#FFD100">¥{{ lastOrder.totalAmount }}</div>
        <div style="margin-top:6px;color:#999;font-size:12px">二维码有效期：{{ qrCountdown }} 秒</div>
        <div style="margin-top:4px;color:#52c41a;font-size:12px">等待顾客付款…</div>
      </div>
      <template #footer>
        <n-space justify="center">
          <n-button @click="cancelQr">取 消</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useMessage } from 'naive-ui'
import { SearchOutline } from '@vicons/ionicons5'
import QRCode from 'qrcode'
import { printReceipt } from '@/utils/receipt'
import request from '@/api/request'
import { getCategories } from '@/api/products'
import { useAuthStore } from '@/stores/auth'

const message = useMessage()
const authStore = useAuthStore()

const categories   = ref([])
const products     = ref([])
const filteredProducts = ref([])
const activeCategory   = ref('all')
const searchKw     = ref('')
const cart         = ref([])
const remark       = ref('')
const paymentType  = ref('CASH')
const cashReceived = ref(0)
const checkoutLoading = ref(false)
const showReceipt  = ref(false)
const lastOrder    = ref({ orderNo: '', totalAmount: '0.00', items: [], paymentType: 'CASH', time: '' })
const showQrModal  = ref(false)
const qrDataUrl    = ref('')
const qrCountdown  = ref(120)
let qrCountTimer   = null
let qrPollTimer    = null

const paymentTypeLabel = { CASH: '现金', ALIPAY: '支付宝', WECHAT: '微信' }

// ── 计算 ────────────────────────────────────────────────
const totalQty = computed(() => cart.value.reduce((s, i) => s + i.qty, 0))
const totalAmount = computed(() => cart.value.reduce((s, i) => s + i.price * i.qty, 0).toFixed(2))
const changeAmount = computed(() => {
  const change = cashReceived.value - Number(totalAmount.value)
  return change >= 0 ? change.toFixed(2) : '0.00'
})

// ── 商品操作 ─────────────────────────────────────────────
const addToCart = (p) => {
  const price = p.discountPrice != null ? Number(p.discountPrice) : Number(p.price)
  const exist = cart.value.find(i => i.id === p.id)
  if (exist) { exist.qty++; return }
  cart.value.push({ id: p.id, name: p.name, price, image: p.image || null, productId: p.id, qty: 1 })
}

const decItem = (item) => {
  if (item.qty <= 1) cart.value = cart.value.filter(i => i.id !== item.id)
  else item.qty--
}

// 搜索/分类同时生效
const applyFilter = () => {
  let list = activeCategory.value === 'all'
    ? products.value
    : products.value.filter(p => String(p.categoryId) === activeCategory.value)
  if (searchKw.value.trim()) {
    const kw = searchKw.value.toLowerCase()
    list = list.filter(p => p.name.toLowerCase().includes(kw))
  }
  filteredProducts.value = list
}
const filterProducts = () => applyFilter()
watch(searchKw, applyFilter)

// ── 结算 ─────────────────────────────────────────────────
const doCheckout = async () => {
  if (!cart.value.length) return
  if (paymentType.value === 'CASH' && cashReceived.value > 0 && cashReceived.value < Number(totalAmount.value)) {
    message.warning('收款金额不足')
    return
  }
  checkoutLoading.value = true
  try {
    const res = await request.post('/merchant/pos/checkout', {
      items: cart.value.map(i => ({
        productId: i.productId,
        name: i.name,
        price: i.price,
        image: i.image,
        qty: i.qty
      })),
      paymentType: paymentType.value,
      remark: remark.value
    })
    lastOrder.value = {
      orderNo: res.orderNo,
      totalAmount: Number(res.totalAmount).toFixed(2),
      items: [...cart.value],
      paymentType: paymentType.value,
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    }
    if (res.needScan) {
      // 需要扫码：获取二维码并轮询支付结果
      await openQrModal(res.orderNo, paymentType.value)
    } else {
      showReceiptAndPrint()
    }
    cart.value = []
    remark.value = ''
    cashReceived.value = 0
  } catch (e) {
    message.error(e.message || '结算失败')
  } finally {
    checkoutLoading.value = false
  }
}

// ── 扫码支付逻辑 ──────────────────────────────────────────
// 收款完成：先自动打印再弹小票弹窗
const showReceiptAndPrint = () => {
  printPosReceipt(lastOrder.value)
  showReceipt.value = true
}

const printPosReceipt = (order) => {
  printReceipt({
    orderNo:     order.orderNo,
    createdAt:   order.time,
    paymentType: order.paymentType,
    addressText: null,   // POS 线下订单无需地址
    remark:      null,
    totalAmount: order.totalAmount,
    deliveryFee: 0,
    items: (order.items || []).map(i => ({
      name:      i.name,
      qty:       i.qty,
      unitPrice: i.price,
      subtotal:  (i.price * i.qty).toFixed(2)
    }))
  })
}
const openQrModal = async (orderNo, type) => {
  qrDataUrl.value = ''
  qrCountdown.value = 120
  showQrModal.value = true
  try {
    const endpoint = type === 'ALIPAY'
      ? `/payment/pos/alipay/qrcode/${orderNo}`
      : `/payment/pos/wechat/qrcode/${orderNo}`
    const data = await request.get(endpoint)
    const codeContent = type === 'ALIPAY' ? data.qrCode : data.codeUrl
    qrDataUrl.value = await QRCode.toDataURL(codeContent, { width: 200, margin: 1 })
  } catch (e) {
    message.error('获取二维码失败：' + e.message)
    showQrModal.value = false
    return
  }
  // 倒计时
  qrCountTimer = setInterval(() => {
    qrCountdown.value--
    if (qrCountdown.value <= 0) cancelQr()
  }, 1000)
  // 轮询支付状态（每 2 秒一次）
  qrPollTimer = setInterval(async () => {
    try {
      const status = await request.get(`/payment/query/${lastOrder.value.orderNo}`)
      if (status.paid) {
        clearQrTimers()
        showQrModal.value = false
        showReceiptAndPrint()
      }
    } catch (_) { /* 忽略轮询报错 */ }
  }, 2000)
}

const clearQrTimers = () => {
  if (qrCountTimer) { clearInterval(qrCountTimer); qrCountTimer = null }
  if (qrPollTimer)  { clearInterval(qrPollTimer);  qrPollTimer  = null }
}

const cancelQr = () => {
  clearQrTimers()
  showQrModal.value = false
  message.info('已取消扫码支付')
}

onUnmounted(() => clearQrTimers())

// ── 初始化 ───────────────────────────────────────────────
onMounted(async () => {
  try {
    const [cats, prods] = await Promise.all([
      getCategories(authStore.merchantId),
      request.get('/merchant/pos/products')
    ])
    categories.value = cats || []
    products.value = Array.isArray(prods) ? prods : (prods?.records || [])
    filteredProducts.value = products.value
  } catch (e) {
    message.error('加载商品失败：' + e.message)
  }
})
</script>

<style scoped>
.pos-wrap {
  height: calc(100vh - 116px);
  display: flex;
  flex-direction: column;
}
.pos-wrap > .n-grid {
  flex: 1;
  min-height: 0;
}
.product-card {
  border: 1.5px solid #f0f0f0;
  border-radius: 10px;
  padding: 10px 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.18s;
  background: #fafafa;
  user-select: none;
}
.product-card:hover { border-color: #FFD100; background: #fffef0; transform: translateY(-2px); box-shadow: 0 4px 12px rgba(255,209,0,0.2); }
.product-card:active { transform: scale(0.97); }
.product-img { height: 88px; display: flex; align-items: center; justify-content: center; overflow: hidden; border-radius: 8px; background: #f5f5f5; }
.product-img img { width: 100%; height: 100%; object-fit: cover; }
.product-placeholder { font-size: 34px; }
.product-name { font-size: 12px; font-weight: 600; margin-top: 6px; color: #333; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.product-price { font-size: 13px; font-weight: 700; color: #FFD100; margin-top: 3px; }
.original-price { font-size: 11px; color: #ccc; text-decoration: line-through; margin-left: 3px; font-weight: 400; }
.cart-item { display: flex; justify-content: space-between; align-items: center; padding: 7px 0; border-bottom: 1px solid #f5f5f5; gap: 8px; }
.cart-name { flex: 1; min-width: 0; font-size: 13px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
/* 小票 */
.receipt { font-size: 13px; }
.receipt-amount { font-size: 44px; font-weight: 900; color: #FFD100; text-align: center; }
.receipt-no { text-align: center; color: #999; font-size: 12px; margin-bottom: 4px; }
.receipt-row { display: flex; justify-content: space-between; padding: 3px 0; }
</style>
