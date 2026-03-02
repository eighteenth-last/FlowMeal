<template>
  <div class="pos-page">
    <n-grid :cols="3" :x-gap="16" style="height:calc(100vh - 130px)">
      <!-- 商品区 (占2列) -->
      <n-gi :span="2">
        <n-card :bordered="false" style="border-radius:12px;height:100%">
          <!-- 分类 Tab -->
          <n-tabs v-model:value="activeCategory" type="bar" @update:value="filterProducts">
            <n-tab name="all">全部</n-tab>
            <n-tab v-for="cat in categories" :key="cat.id" :name="String(cat.id)">
              {{ cat.name }}
            </n-tab>
          </n-tabs>

          <!-- 商品网格 -->
          <n-grid :cols="4" :x-gap="12" :y-gap="12" style="margin-top:12px;overflow-y:auto;max-height:calc(100vh - 240px)">
            <n-gi v-for="p in filteredProducts" :key="p.id">
              <div class="product-card" @click="addToCart(p)">
                <div class="product-img">
                  <img v-if="p.imageUrl" :src="p.imageUrl" :alt="p.name" />
                  <div v-else class="product-placeholder">🍱</div>
                </div>
                <div class="product-name">{{ p.name }}</div>
                <div class="product-price">¥{{ p.price }}</div>
              </div>
            </n-gi>
          </n-grid>
        </n-card>
      </n-gi>

      <!-- 购物车区（右侧）-->
      <n-gi>
        <n-card :bordered="false" style="border-radius:12px;height:100%;display:flex;flex-direction:column">
          <template #header>
            <n-space justify="space-between" align="center">
              <span>收银台</span>
              <n-button text type="error" size="small" @click="cart = []">清空</n-button>
            </n-space>
          </template>

          <!-- 购物车列表 -->
          <div style="flex:1;overflow-y:auto">
            <n-empty v-if="cart.length === 0" description="暂无商品" style="margin-top:40px" />
            <div v-for="item in cart" :key="item.id" class="cart-item">
              <span class="cart-name">{{ item.name }}</span>
              <n-space align="center">
                <n-button circle size="tiny" @click="decItem(item)">-</n-button>
                <span style="min-width:20px;text-align:center">{{ item.qty }}</span>
                <n-button circle size="tiny" type="primary" @click="item.qty++">+</n-button>
                <span style="color:#FFD100;font-weight:600;min-width:52px;text-align:right">
                  ¥{{ (item.price * item.qty).toFixed(2) }}
                </span>
              </n-space>
            </div>
          </div>

          <n-divider style="margin:12px 0" />

          <!-- 合计 & 结算 -->
          <div>
            <n-space justify="space-between" style="margin-bottom:12px">
              <span style="font-size:15px">合计</span>
              <span style="font-size:22px;font-weight:800;color:#FFD100">¥{{ totalAmount }}</span>
            </n-space>
            <n-input v-model:value="remark" placeholder="备注（可选）" style="margin-bottom:12px" />
            <n-button
              type="primary"
              block
              size="large"
              :disabled="cart.length === 0"
              @click="showCheckout = true"
            >
              收 款 ¥{{ totalAmount }}
            </n-button>
          </div>
        </n-card>
      </n-gi>
    </n-grid>

    <!-- 结算确认弹窗 -->
    <n-modal v-model:show="showCheckout" title="确认收款" preset="card" style="width:360px">
      <div style="text-align:center">
        <div style="font-size:48px;font-weight:900;color:#FFD100">¥{{ totalAmount }}</div>
        <div style="color:#999;margin:8px 0">现金 / 扫码收款</div>
      </div>
      <n-list style="margin-top:16px">
        <n-list-item v-for="item in cart" :key="item.id" style="padding:4px 0">
          {{ item.name }} × {{ item.qty }}
          <template #suffix>¥{{ (item.price * item.qty).toFixed(2) }}</template>
        </n-list-item>
      </n-list>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showCheckout = false">取消</n-button>
          <n-button type="primary" @click="confirmCheckout">确认收款</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { getProducts, getCategories } from '@/api/products'
import { useAuthStore } from '@/stores/auth'

const message = useMessage()
const authStore = useAuthStore()
const categories = ref([])
const products = ref([])
const filteredProducts = ref([])
const activeCategory = ref('all')
const cart = ref([])
const remark = ref('')
const showCheckout = ref(false)

const totalAmount = computed(() =>
  cart.value.reduce((s, i) => s + i.price * i.qty, 0).toFixed(2)
)

const addToCart = (p) => {
  const exist = cart.value.find(i => i.id === p.id)
  if (exist) { exist.qty++; return }
  cart.value.push({ ...p, qty: 1 })
}

const decItem = (item) => {
  if (item.qty <= 1) {
    cart.value = cart.value.filter(i => i.id !== item.id)
  } else {
    item.qty--
  }
}

const filterProducts = (cat) => {
  filteredProducts.value = cat === 'all'
    ? products.value
    : products.value.filter(p => String(p.categoryId) === cat)
}

const confirmCheckout = () => {
  message.success(`收款成功 ¥${totalAmount.value}`)
  cart.value = []
  remark.value = ''
  showCheckout.value = false
}

onMounted(async () => {
  try {
    const [cats, prods] = await Promise.all([
      getCategories(authStore.merchantId),
      getProducts({ page: 1, size: 200 })
    ])
    categories.value = cats || []
    products.value = prods?.records || []
    filteredProducts.value = products.value
  } catch (e) {
    message.error('加载商品失败：' + e.message)
  }
})
</script>

<style scoped>
.pos-page { height: calc(100vh - 130px); }
.product-card {
  border: 1.5px solid #f0f0f0;
  border-radius: 10px;
  padding: 10px 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  background: #fafafa;
}
.product-card:hover { border-color: #FFD100; background: #fffef0; transform: translateY(-2px); }
.product-img { height: 70px; display:flex; align-items:center; justify-content:center; overflow:hidden; border-radius:6px; }
.product-img img { width: 100%; height: 100%; object-fit: cover; }
.product-placeholder { font-size: 36px; }
.product-name { font-size: 12px; font-weight: 600; margin-top: 6px; color: #333; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.product-price { font-size: 13px; font-weight: 700; color: #FFD100; margin-top: 2px; }
.cart-item { display:flex; justify-content:space-between; align-items:center; padding: 8px 0; border-bottom: 1px solid #f5f5f5; }
.cart-name { font-size: 13px; flex: 1; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; max-width: 90px; }
</style>
