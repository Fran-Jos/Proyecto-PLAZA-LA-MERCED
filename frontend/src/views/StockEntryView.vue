<script setup>
import { ref, onMounted } from 'vue'
import { ProductService } from '../service/api'
import InputText from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'
import Button from 'primevue/button'
import ThemeToggle from '../components/ThemeToggle.vue'
import Message from 'primevue/message'

const products = ref([])
const barcodeQuery = ref('')
const searchInput = ref(null)
const selectedProduct = ref(null)
const quantityToAdd = ref(1)
const history = ref([])
const loading = ref(false)

onMounted(async () => {
  loadProducts()
  focusSearch()
})

const loadProducts = async () => {
  const res = await ProductService.getAll()
  products.value = res.data
}

const focusSearch = () => {
  setTimeout(() => {
    if (searchInput.value) {
      searchInput.value.$el.querySelector('input')?.focus()
    }
  }, 300)
}

const handleScan = () => {
  if (!barcodeQuery.value) return
  
  const match = products.value.find(p => p.barcode === barcodeQuery.value)
  if (match) {
    selectedProduct.value = match
    quantityToAdd.value = 1
    // Si queremos que sea ULTRA rápido, podríamos auto-guardar aquí con cantidad 1
    // Pero es mejor confirmar para evitar errores
  }
  barcodeQuery.value = ''
}

const saveEntry = async () => {
  if (!selectedProduct.value || quantityToAdd.value === 0) return
  
  loading.value = true
  try {
    const updated = await ProductService.updateStock(selectedProduct.value.id, quantityToAdd.value)
    
    // Agregar al historial local
    history.value.unshift({
      id: Date.now(),
      name: selectedProduct.value.name,
      added: quantityToAdd.value,
      newStock: updated.data.stock,
      time: new Date().toLocaleTimeString()
    })
    
    // Limpiar selección
    selectedProduct.value = null
    quantityToAdd.value = 1
    await loadProducts()
    focusSearch()
  } catch (err) {
    alert(err.response?.data?.message || 'Error al actualizar stock')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-950 p-6 md:p-12 transition-colors duration-300">
    <div class="max-w-6xl mx-auto space-y-8">
      
      <header class="flex justify-between items-center">
        <div>
          <h1 class="text-4xl font-black text-gray-900 dark:text-white tracking-tight uppercase">Entrada de Mercadería</h1>
          <p class="text-gray-500 dark:text-gray-400 mt-1 font-medium">Carga rápida de stock mediante escaneo.</p>
        </div>
        <div class="flex items-center gap-4">
          <ThemeToggle />
          <router-link to="/">
            <Button icon="pi pi-home" label="Volver" severity="secondary" rounded class="font-bold" />
          </router-link>
        </div>
      </header>

      <div class="grid md:grid-cols-2 gap-8">
        <!-- Panel de Escaneo -->
        <div class="space-y-6">
          <div class="bg-white dark:bg-gray-900 p-8 rounded-3xl shadow-xl border border-gray-100 dark:border-gray-800">
            <label class="block text-sm font-black text-gray-400 uppercase tracking-widest mb-4">Paso 1: Escanear Producto</label>
            <div class="relative">
              <i class="pi pi-barcode absolute left-4 top-1/2 -translate-y-1/2 text-3xl text-gray-300" />
              <InputText ref="searchInput" v-model="barcodeQuery" @input="handleScan"
                         class="w-full pl-16 h-16 text-2xl font-mono rounded-2xl border-2 border-gray-100 dark:border-gray-800 bg-gray-50 dark:bg-gray-950 dark:text-white focus:border-blue-500 focus:ring-0 transition-all"
                         placeholder="ESCANEAR CÓDIGO..." />
            </div>
          </div>

          <transition name="slide-up">
            <div v-if="selectedProduct" class="bg-blue-600 rounded-3xl p-8 text-white shadow-2xl space-y-6">
              <div class="flex justify-between items-start">
                <div>
                  <span class="text-blue-200 text-xs font-black uppercase tracking-widest">Producto Detectado</span>
                  <h2 class="text-3xl font-black leading-tight">{{ selectedProduct.name }}</h2>
                  <p class="text-blue-100 font-bold opacity-80">Stock actual: {{ selectedProduct.stock }}</p>
                </div>
                <div class="bg-white/20 p-3 rounded-2xl backdrop-blur-md">
                  <i class="pi pi-box text-3xl" />
                </div>
              </div>

              <div class="space-y-4">
                <label class="block text-sm font-black uppercase tracking-widest">Paso 2: Cantidad a Ingresar</label>
                <div class="flex gap-4">
                   <InputNumber v-model="quantityToAdd" showButtons buttonLayout="horizontal" 
                                :min="1" class="flex-1"
                                inputClass="h-16 text-center text-3xl font-black rounded-2xl border-0 !bg-white/20 !text-white"
                                incrementButtonClass="!bg-white/10 !border-0 !text-white !w-16 rounded-r-2xl"
                                decrementButtonClass="!bg-white/10 !border-0 !text-white !w-16 rounded-l-2xl"
                                incrementButtonIcon="pi pi-plus" decrementButtonIcon="pi pi-minus" />
                </div>
                <Button label="CONFIRMAR INGRESO" 
                        class="w-full h-16 !bg-white !text-blue-600 !border-0 !font-black !text-lg !rounded-2xl shadow-xl hover:scale-[1.02] active:scale-95 transition-all"
                        :loading="loading" @click="saveEntry" />
                <Button label="CANCELAR" text class="w-full !text-white !font-bold opacity-60" @click="selectedProduct = null" />
              </div>
            </div>
          </transition>
        </div>

        <!-- Historial de Carga -->
        <div class="bg-white dark:bg-gray-900 rounded-3xl shadow-xl border border-gray-100 dark:border-gray-800 overflow-hidden flex flex-col">
          <div class="p-6 border-b border-gray-50 dark:border-gray-800 flex justify-between items-center bg-gray-50/50 dark:bg-gray-950/50">
            <h3 class="font-black text-gray-800 dark:text-white uppercase tracking-tighter">Cargas Recientes</h3>
            <span class="bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 px-3 py-1 rounded-full text-xs font-black">{{ history.length }}</span>
          </div>
          
          <div class="flex-1 overflow-y-auto p-4 space-y-3 min-h-[400px]">
            <div v-if="history.length === 0" class="h-full flex flex-col items-center justify-center text-gray-300 dark:text-gray-700 opacity-40">
              <i class="pi pi-history text-6xl mb-4" />
              <p class="font-bold uppercase tracking-widest">Sin actividad</p>
            </div>

            <div v-for="item in history" :key="item.id" 
                 class="flex justify-between items-center p-4 bg-gray-50 dark:bg-gray-950 rounded-2xl border border-transparent hover:border-gray-200 dark:hover:border-gray-800 transition-all group">
              <div class="flex items-center gap-4">
                <div class="w-10 h-10 rounded-xl bg-emerald-100 dark:bg-emerald-900/30 text-emerald-600 dark:text-emerald-400 flex items-center justify-center font-black">
                  +{{ item.added }}
                </div>
                <div>
                  <div class="font-bold text-gray-800 dark:text-gray-200">{{ item.name }}</div>
                  <div class="text-[10px] text-gray-400 font-bold uppercase tracking-wider">{{ item.time }} • Stock final: {{ item.newStock }}</div>
                </div>
              </div>
              <i class="pi pi-check-circle text-emerald-500 opacity-0 group-hover:opacity-100 transition-opacity" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(30px) scale(0.95);
}

.slide-up-leave-to {
  opacity: 0;
  transform: translateY(-20px) scale(0.95);
}
</style>
