const APP_EVENT = 'pos:data-changed'

export const DataEvents = {
  SALE_CREATED: 'sale-created',
  CASH_OPENED: 'cash-opened',
  CASH_CLOSED: 'cash-closed'
}

export const emitDataChanged = (type, payload = {}) => {
  window.dispatchEvent(new CustomEvent(APP_EVENT, { detail: { type, payload, at: Date.now() } }))
}

export const subscribeDataChanged = (callback) => {
  const handler = (event) => callback(event.detail)
  window.addEventListener(APP_EVENT, handler)
  return () => window.removeEventListener(APP_EVENT, handler)
}
