<script>
export default {
  onLaunch() {
    console.log('FlowMeal 骑手端启动')
  }
}
</script>

<style>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css');

page {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background-color: #f5f5f5;
  color: #1a1a1a;
  font-size: 28rpx;
}

.fa, .fas, .far, .fab {
  font-family: 'Font Awesome 6 Free', 'Font Awesome 6 Brands' !important;
}

/* 骑手端深色主题 */
.bg-dark { background-color: #1a1a1a; }
.bg-primary { background-color: #FFD100; }
.text-primary { color: #FFD100; }
.bg-white { background-color: #ffffff; }
.text-white { color: #ffffff; }
.text-gray { color: #999999; }
.text-black { color: #1a1a1a; }

.flex { display: flex; }
.flex-col { flex-direction: column; }
.flex-row { flex-direction: row; }
.items-center { align-items: center; }
.justify-center { justify-content: center; }
.justify-between { justify-content: space-between; }
.flex-1 { flex: 1; }
.text-center { text-align: center; }
.font-bold { font-weight: bold; }
.rounded-full { border-radius: 999rpx; }

.px-30 { padding-left: 30rpx; padding-right: 30rpx; }
.py-20 { padding-top: 20rpx; padding-bottom: 20rpx; }
.mt-20 { margin-top: 20rpx; }
.mb-20 { margin-bottom: 20rpx; }
.gap-20 { gap: 20rpx; }

.btn-primary {
  background: linear-gradient(135deg, #FFD100 0%, #FFE132 100%);
  color: #1a1a1a;
  border-radius: 999rpx;
  padding: 24rpx 0;
  text-align: center;
  font-weight: bold;
  font-size: 32rpx;
}

.btn-dark {
  background-color: #1a1a1a;
  color: #ffffff;
  border-radius: 999rpx;
  padding: 24rpx 0;
  text-align: center;
  font-weight: bold;
  font-size: 32rpx;
}

.card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}

.safe-bottom {
  padding-bottom: env(safe-area-inset-bottom);
}
</style>
