<script setup>
import { ref, onMounted } from 'vue'
import { ProductService } from '../service/api'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'
import ThemeToggle from '../components/ThemeToggle.vue'

const products = ref([])
const scannedProduct = ref(null)
const barcodeQuery = ref('')
const searchInput = ref(null)
const error = ref(null)

onMounted(async () => {
  const res = await ProductService.getAll()
  products.value = res.data
  focusSearch()
})

const focusSearch = () => {
  setTimeout(() => {
    if (searchInput.value) {
      searchInput.value.$el.querySelector('input')?.focus()
    }
  }, 300)
}

const handleScan = () => {
  if (!barcodeQuery.value) return
  
  error.value = null
  const match = products.value.find(p => p.barcode === barcodeQuery.value)
  
  if (match) {
    scannedProduct.value = match
    // Resetear después de 5 segundos para el siguiente cliente
    setTimeout(() => {
      if (scannedProduct.value?.id === match.id) {
        scannedProduct.value = null
      }
    }, 5000)
  } else {
    error.value = 'Producto no encontrado'
    scannedProduct.value = null
  }
  
  barcodeQuery.value = ''
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-950 flex flex-col items-center justify-center p-6 transition-colors duration-300">
    <div class="absolute top-6 right-6 flex gap-4">
      <ThemeToggle />
      <router-link to="/">
        <Button icon="pi pi-home" severity="secondary" rounded text />
      </router-link>
    </div>

    <div class="w-full max-w-4xl text-center space-y-12">
      <header>
        <h1 class="text-5xl font-black text-gray-900 dark:text-white tracking-tighter mb-4 uppercase">Verificador de Precios</h1>
        <p class="text-xl text-gray-500 dark:text-gray-400">Escanee el código de barras para ver el precio actual</p>
      </header>

      <div class="relative max-w-lg mx-auto">
        <InputText ref="searchInput" v-model="barcodeQuery" @input="handleScan"
                   class="w-full h-20 text-center text-3xl font-mono rounded-3xl border-0 shadow-2xl ring-2 ring-blue-500 bg-white dark:bg-gray-900 dark:text-white focus:ring-4 focus:ring-blue-600 transition-all"
                   placeholder="ESCANEE AQUÍ" />
        <div class="absolute -bottom-10 left-0 right-0 animate-bounce">
          <i class="pi pi-arrow-up text-blue-500 text-2xl" />
        </div>
      </div>

      <div class="h-80 flex items-center justify-center">
        <transition name="fade-scale" mode="out-in">
          <div v-if="scannedProduct" :key="scannedProduct.id" class="w-full bg-white dark:bg-gray-900 rounded-[3rem] shadow-2xl p-12 border-b-8 border-blue-600 dark:border-blue-500 flex flex-col items-center gap-6">
            <h2 class="text-4xl font-black text-gray-800 dark:text-gray-100 uppercase tracking-tight">{{ scannedProduct.name }}</h2>
            <div class="flex items-baseline gap-2">
              <span class="text-8xl font-black text-blue-600 dark:text-blue-400 tracking-tighter">${{ scannedProduct.price.toFixed(2) }}</span>
            </div>
            <div class="px-6 py-2 bg-emerald-100 dark:bg-emerald-900/30 text-emerald-700 dark:text-emerald-400 rounded-full font-bold text-sm uppercase tracking-widest">
              Stock disponible: {{ scannedProduct.stock }}
            </div>
          </div>
          
          <div v-else-if="error" class="bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 px-8 py-4 rounded-2xl font-bold text-xl border border-red-100 dark:border-red-900/30">
            {{ error }}
          </div>
          
          <div v-else class="text-gray-300 dark:text-gray-800 flex flex-col items-center gap-4 opacity-40">
            <i class="pi pi-barcode text-[10rem]" />
            <span class="text-2xl font-bold uppercase tracking-[0.5rem]">Esperando Escaneo</span>
          </div>
        </transition>
      </div>
    </div>
  </div>
</template>

<style scoped>
.fade-scale-enter-active,
.fade-scale-leave-active {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.fade-scale-enter-from {
  opacity: 0;
  transform: scale(0.9) translateY(20px);
}

.fade-scale-leave-to {
  opacity: 0;
  transform: scale(0.9);
}
</style>
