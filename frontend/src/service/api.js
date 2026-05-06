import axios from 'axios'
import { useAuthStore } from '../store/auth'

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

const api = axios.create({
  baseURL: API_URL
})

api.interceptors.request.use((config) => {
  const authStore = useAuthStore()
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`
  }
  return config
})

export const ProductService = {
  getAll() {
    return api.get('/products')
  },
  save(product) {
    if (product.id) {
      return api.put(`/products/${product.id}`, product)
    }
    return api.post('/products', product)
  },
  delete(id) {
    return api.delete(`/products/${id}`)
  },
  getCategories() {
    return api.get('/products/categories')
  },
  saveCategory(category) {
    return api.post('/products/categories', category)
  }
}

export const SaleService = {
  create(saleData) {
    return api.post('/sales', saleData)
  },
  getAll() {
    return api.get('/sales')
  }
}

export const CashService = {
  getActive(userId) {
    return api.get(`/cash/active/${userId}`)
  },
  open(data) {
    return api.post('/cash/open', data)
  },
  close(data) {
    return api.post('/cash/close', data)
  }
}
