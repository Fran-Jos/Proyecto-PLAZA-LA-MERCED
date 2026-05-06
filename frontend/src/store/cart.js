import { defineStore } from 'pinia'

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: []
  }),
  getters: {
    total: (state) => {
      return state.items.reduce((acc, item) => acc + (item.price * item.quantity), 0)
    },
    count: (state) => {
      return state.items.reduce((acc, item) => acc + item.quantity, 0)
    }
  },
  actions: {
    addItem(product) {
      const existing = this.items.find(i => i.id === product.id)
      if (existing) {
        existing.quantity++
      } else {
        this.items.push({
          id: product.id,
          name: product.name,
          price: product.price,
          quantity: 1
        })
      }
    },
    removeItem(productId) {
      const index = this.items.findIndex(i => i.id === productId)
      if (index > -1) {
        if (this.items[index].quantity > 1) {
          this.items[index].quantity--
        } else {
          this.items.splice(index, 1)
        }
      }
    },
    clear() {
      this.items = []
    }
  }
})
