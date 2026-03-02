import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useCartStore = defineStore('cart', () => {
  const items = ref([])
  const merchantId = ref(null)
  const merchantName = ref('')

  const totalCount = computed(() => items.value.reduce((sum, item) => sum + item.quantity, 0))
  const totalPrice = computed(() =>
    items.value.reduce((sum, item) => {
      const price = item.discountPrice || item.price
      return sum + price * item.quantity
    }, 0)
  )

  const addItem = (product, mid, mname) => {
    // 如果切换了商家，先清空
    if (merchantId.value && merchantId.value !== mid) {
      items.value = []
    }
    merchantId.value = mid
    merchantName.value = mname

    const existing = items.value.find(i => i.productId === product.id)
    if (existing) {
      existing.quantity++
    } else {
      items.value.push({
        productId: product.id,
        productName: product.name,
        productImage: product.image,
        price: product.price,
        discountPrice: product.discountPrice,
        quantity: 1,
        stock: product.stock
      })
    }
  }

  const updateQty = (productId, qty) => {
    const item = items.value.find(i => i.productId === productId)
    if (item) {
      if (qty <= 0) {
        items.value = items.value.filter(i => i.productId !== productId)
      } else {
        item.quantity = qty
      }
    }
  }

  const clear = () => {
    items.value = []
    merchantId.value = null
    merchantName.value = ''
  }

  return { items, merchantId, merchantName, totalCount, totalPrice, addItem, updateQty, clear }
})
