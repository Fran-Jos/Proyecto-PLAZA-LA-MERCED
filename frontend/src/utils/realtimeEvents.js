const APP_EVENT = 'pos:data-changed'
const channel = new BroadcastChannel(APP_EVENT)

export const DataEvents = {
  SALE_CREATED: 'sale-created',
  CASH_OPENED: 'cash-opened',
  CASH_CLOSED: 'cash-closed'
}

export const emitDataChanged = (type, payload = {}) => {
  const data = { type, payload, at: Date.now() }
  // Emit to current window
  window.dispatchEvent(new CustomEvent(APP_EVENT, { detail: data }))
  // Emit to other tabs
  channel.postMessage(data)
}

export const subscribeDataChanged = (callback) => {
  const handler = (event) => callback(event.detail || event.data)
  window.addEventListener(APP_EVENT, handler)
  channel.addEventListener('message', handler)
  
  return () => {
    window.removeEventListener(APP_EVENT, handler)
    channel.removeEventListener('message', handler)
  }
}
