import { defineStore } from 'pinia'

const STORAGE_KEY = 'localcontrol_accounts'

const load = () => {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
  } catch {
    return []
  }
}

const save = (accounts) => localStorage.setItem(STORAGE_KEY, JSON.stringify(accounts))

export const useAccountsStore = defineStore('accounts', {
  state: () => ({
    accounts: load()
  }),
  getters: {
    pending: state => state.accounts.filter(a => a.status === 'PENDING'),
    paid: state => state.accounts.filter(a => a.status === 'PAID')
  },
  actions: {
    createAccount(clientName) {
      const account = { id: Date.now(), clientName, items: [], status: 'PENDING', createdAt: Date.now() }
      this.accounts.unshift(account)
      save(this.accounts)
      return account
    },
    addItem(accountId, product) {
      const account = this.accounts.find(a => a.id === accountId)
      if (!account) return
      const existing = account.items.find(i => i.id === product.id)
      if (existing) existing.quantity += 1
      else account.items.push({ ...product, quantity: 1 })
      save(this.accounts)
    },
    removeItem(accountId, productId) {
      const account = this.accounts.find(a => a.id === accountId)
      if (!account) return
      const existing = account.items.find(i => i.id === productId)
      if (!existing) return
      if (existing.quantity > 1) existing.quantity -= 1
      else account.items = account.items.filter(i => i.id !== productId)
      save(this.accounts)
    },
    markPaid(accountId) {
      const account = this.accounts.find(a => a.id === accountId)
      if (!account) return
      account.status = 'PAID'
      account.paidAt = Date.now()
      save(this.accounts)
    }
  }
})
