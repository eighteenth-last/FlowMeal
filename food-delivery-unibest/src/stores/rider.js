import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useRiderStore = defineStore('rider', () => {
  // Initialize from storage or default to true
  const isOnline = ref(uni.getStorageSync('fm_rider_online') === '' ? true : uni.getStorageSync('fm_rider_online'))

  const setOnline = (status) => {
    isOnline.value = status
    uni.setStorageSync('fm_rider_online', status)
  }

  const toggleOnline = () => {
    isOnline.value = !isOnline.value
    uni.setStorageSync('fm_rider_online', isOnline.value)
  }

  return { isOnline, setOnline, toggleOnline }
})
