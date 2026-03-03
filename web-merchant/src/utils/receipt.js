/**
 * 小票打印工具
 * @param {Object} o
 * @param {string}  o.orderNo
 * @param {string}  [o.createdAt]
 * @param {string}  [o.paymentType]   CASH | ALIPAY | WECHAT
 * @param {string}  [o.addressText]   格式化后的地址文本，传 null 则不显示
 * @param {string}  [o.remark]
 * @param {number}  o.totalAmount     合计
 * @param {number}  [o.deliveryFee]
 * @param {Array}   o.items           [{name, qty, unitPrice, subtotal}]
 */
export function printReceipt(o) {
  const PAYMENT_LABEL = { CASH: '现金', ALIPAY: '支付宝', WECHAT: '微信支付' }

  const itemsHtml = (o.items || []).map(i => `
    <tr>
      <td style="padding:4px 2px;border-bottom:1px dashed #ddd">${i.name}</td>
      <td style="padding:4px 2px;border-bottom:1px dashed #ddd;text-align:center">x${i.qty}</td>
      <td style="padding:4px 2px;border-bottom:1px dashed #ddd;text-align:right">¥${Number(i.unitPrice).toFixed(2)}</td>
      <td style="padding:4px 2px;border-bottom:1px dashed #ddd;text-align:right">¥${Number(i.subtotal).toFixed(2)}</td>
    </tr>`).join('')

  const deliveryRow = Number(o.deliveryFee) > 0
    ? `<tr><td colspan="3" style="text-align:right;font-size:11px;color:#666">配送费：</td>
       <td style="text-align:right;font-size:11px;color:#666">¥${Number(o.deliveryFee).toFixed(2)}</td></tr>`
    : ''

  const addrLine  = o.addressText ? `<p class="meta">收货信息：${o.addressText}</p>` : ''
  const remarkLine = o.remark ? `<p class="meta">备注：${o.remark}</p>` : ''

  const html = `<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<title>小票-${o.orderNo}</title>
<style>
  * { margin:0; padding:0; box-sizing:border-box; }
  body { font-family:'Courier New',monospace; font-size:13px; width:300px; margin:0 auto; padding:12px; color:#000; }
  .center { text-align:center; }
  .logo { font-size:22px; font-weight:bold; margin-bottom:2px; }
  .divider { border:none; border-top:1px dashed #999; margin:8px 0; }
  .divider-solid { border:none; border-top:2px solid #000; margin:8px 0; }
  table { width:100%; border-collapse:collapse; font-size:12px; }
  th { font-size:11px; color:#666; padding:4px 2px; border-bottom:1px solid #000; text-align:left; }
  th:nth-child(n+2) { text-align:center; }
  th:last-child { text-align:right; }
  .total-row td { font-weight:bold; font-size:14px; padding-top:8px; }
  .meta { font-size:11px; color:#555; margin:3px 0; line-height:1.5; }
  .footer { margin-top:12px; font-size:11px; color:#888; text-align:center; }
  @media print {
    @page { margin:4mm; size:80mm auto; }
    body { width:72mm; }
  }
</style>
</head>
<body>
  <div class="center">
    <div class="logo">FlowMeal</div>
    <div style="font-size:11px;color:#666">订单小票</div>
  </div>
  <hr class="divider-solid">
  <p class="meta">订单号：${o.orderNo}</p>
  <p class="meta">下单时间：${o.createdAt || ''}</p>
  <p class="meta">支付方式：${PAYMENT_LABEL[o.paymentType] || o.paymentType || '-'}</p>
  ${addrLine}
  ${remarkLine}
  <hr class="divider">
  <table>
    <thead><tr>
      <th>商品</th>
      <th style="text-align:center">数量</th>
      <th style="text-align:right">单价</th>
      <th style="text-align:right">小计</th>
    </tr></thead>
    <tbody>${itemsHtml}</tbody>
    <tfoot>
      ${deliveryRow}
      <tr class="total-row">
        <td colspan="3" style="text-align:right;padding-top:8px">合计：</td>
        <td style="text-align:right;padding-top:8px">¥${Number(o.totalAmount).toFixed(2)}</td>
      </tr>
    </tfoot>
  </table>
  <hr class="divider">
  <div class="footer">感谢惠顾，期待再次光临！</div>
</body>
</html>`

  const blob = new Blob([html], { type: 'text/html;charset=utf-8' })
  const url  = URL.createObjectURL(blob)
  const a    = document.createElement('a')
  a.href     = url
  a.download = `小票-${o.orderNo}.html`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  setTimeout(() => URL.revokeObjectURL(url), 5000)
}
