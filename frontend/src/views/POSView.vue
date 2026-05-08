<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { ProductService, SaleService, CashService } from '../service/api'
import { useCartStore } from '../store/cart'
import { useAuthStore } from '../store/auth'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Card from 'primevue/card'
import Dialog from 'primevue/dialog'
import Message from 'primevue/message'
import Badge from 'primevue/badge'
import ThemeToggle from '../components/ThemeToggle.vue'
import { emitDataChanged, DataEvents } from '../utils/realtimeEvents'

const products = ref([])
const categories = ref([])
const activeSession = ref(null)
const searchQuery = ref('')
const searchInput = ref(null)
const showPaymentDialog = ref(false)
const paymentMethod = ref('CASH')
const referenceCode = ref('')
const loading = ref(false)
const saleSuccess = ref(false)
const flashState = ref(null) // 'success' or 'error'

const cartStore = useCartStore()
const authStore = useAuthStore()

onMounted(async () => {
  loadData()
  focusSearch()
})

// Asegurar foco al cerrar diálogos
watch(showPaymentDialog, (val) => {
  if (!val) focusSearch()
})

const focusSearch = () => {
  setTimeout(() => {
    if (searchInput.value) {
      searchInput.value.$el.querySelector('input')?.focus()
    }
  }, 300)
}

const loadData = async () => {
  try {
    const [prodRes, catRes, cashRes] = await Promise.all([
      ProductService.getAll(),
      ProductService.getCategories(),
      CashService.getActive()
    ])
    products.value = prodRes.data
    categories.value = catRes.data
    activeSession.value = cashRes.data
  } catch (err) {
    console.error('Error al cargar datos', err)
  }
}

// Lógica de escaneo: si el código coincide exactamente, se agrega al carrito
const handleSearchInput = () => {
  if (!searchQuery.value) return
  
  const exactMatch = products.value.find(p => p.barcode === searchQuery.value)
  if (exactMatch && activeSession.value) {
    cartStore.addItem(exactMatch)
    searchQuery.value = '' // Limpiar para el siguiente escaneo
  }
}

const handleSearchEnter = () => {
  // Al presionar Enter (que lo hace el lector), intentamos buscar coincidencia exacta
  const exactMatch = products.value.find(p => 
    p.barcode === searchQuery.value || 
    p.name.toLowerCase() === searchQuery.value.toLowerCase()
  )
  if (exactMatch && activeSession.value) {
    cartStore.addItem(exactMatch)
    searchQuery.value = ''
  }
}

const filteredProducts = computed(() => {
  let filtered = products.value
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(p => 
      p.name.toLowerCase().includes(query) ||
      p.barcode?.includes(query)
    )
  }
  
  // Ordenar: favoritos primero, luego por nombre
  return [...filtered].sort((a, b) => {
    if (a.favorite && !b.favorite) return -1
    if (!a.favorite && b.favorite) return 1
    return a.name.localeCompare(b.name)
  })
})

const toggleFavorite = async (product, event) => {
  event.stopPropagation()
  try {
    const updatedProduct = await ProductService.toggleFavorite(product.id)
    const index = products.value.findIndex(p => p.id === product.id)
    if (index !== -1) {
      products.value[index].favorite = updatedProduct.data.favorite
    }
  } catch (err) {
    console.error('Error al marcar favorito', err)
  }
}

const handleProcessPayment = async () => {
  if (!activeSession.value) {
    alert('No hay una caja abierta. Por favor abra una caja antes de vender.')
    return
  }
  
  loading.value = true
  try {
    const saleData = {
      items: cartStore.items.map(i => ({ productId: i.id, quantity: i.quantity })),
      paymentMethod: paymentMethod.value,
      referenceCode: referenceCode.value,
      userId: authStore.user.id
    }
    await SaleService.create(saleData)
    emitDataChanged(DataEvents.SALE_CREATED)
    flashState.value = 'success'
    setTimeout(() => { flashState.value = null }, 1000)
    saleSuccess.value = true
    cartStore.clear()
    referenceCode.value = ''
    setTimeout(() => {
      saleSuccess.value = false
      showPaymentDialog.value = false
      focusSearch() // Volver foco al buscador
    }, 2000)
    // Recargar productos para actualizar stock
    await loadData()
  } catch (err) {
    flashState.value = 'error'
    setTimeout(() => { flashState.value = null }, 1000)
    alert(err.response?.data?.message || 'Error al procesar la venta')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="flex h-screen bg-gray-50 dark:bg-gray-950 overflow-hidden font-sans text-gray-900 dark:text-gray-100 transition-colors duration-300 relative">
    
    <!-- Flash Overlay -->
    <transition name="fade">
      <div v-if="flashState" 
           :class="['fixed inset-0 z-[9999] pointer-events-none transition-all duration-300', 
                    flashState === 'success' ? 'bg-green-500/40' : 'bg-red-500/40']">
      </div>
    </transition>

    <!-- Area de Productos -->
    <div class="flex-1 flex flex-col p-4 md:p-6 overflow-hidden">
      <header class="flex justify-between items-center mb-6">
        <h1 class="text-2xl font-black text-gray-800 dark:text-white tracking-tight">Terminal POS</h1>
        <div class="flex items-center gap-3">
          <div v-if="activeSession" class="flex items-center gap-2 bg-emerald-50 dark:bg-emerald-900/30 text-emerald-700 dark:text-emerald-400 px-3 py-1.5 rounded-full border border-emerald-100 dark:border-emerald-800 shadow-sm">
            <div class="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></div>
            <span class="text-[10px] font-bold uppercase tracking-wider">Caja: {{ activeSession.name }}</span>
          </div>
          <Button label="Cuenta" icon="pi pi-users" severity="secondary" outlined @click="$router.push('/accounts?from=pos')" />
          <ThemeToggle />
          <router-link to="/">
            <Button icon="pi pi-home" text rounded severity="secondary" class="dark:text-gray-300" />
          </router-link>
        </div>
      </header>

      <div class="mb-6 relative">
        <i class="pi pi-search absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
        <InputText ref="searchInput" v-model="searchQuery" placeholder="Buscar producto o código..." 
                   @input="handleSearchInput" @keyup.enter="handleSearchEnter"
                   class="w-full pl-12 h-14 rounded-2xl border-0 shadow-sm ring-1 ring-gray-100 dark:ring-gray-800 bg-white dark:bg-gray-900 focus:ring-2 focus:ring-blue-500 transition-all text-lg dark:text-white" />
      </div>

      <div v-if="!activeSession" class="mb-6">
        <Message severity="warn" class="rounded-xl border border-amber-100 bg-amber-50 dark:bg-amber-900/20 dark:border-amber-900/30">
          Debe abrir una caja para realizar ventas.
        </Message>
      </div>

      <div class="grid grid-cols-2 md:grid-cols-3 xl:grid-cols-5 gap-4 overflow-y-auto pb-10 pr-2">
        <div v-for="product in filteredProducts" :key="product.id" 
             @click="activeSession && cartStore.addItem(product)"
             :class="['group cursor-pointer transition-all active:scale-95 bg-white dark:bg-gray-900 p-4 rounded-2xl shadow-sm border border-gray-100 dark:border-gray-800 hover:border-blue-400 dark:hover:border-blue-600 hover:shadow-md relative overflow-hidden', !activeSession ? 'opacity-50 cursor-not-allowed' : '']">
          
          <div class="flex flex-col h-full justify-between">
            <div>
               <div class="flex justify-between items-start mb-1">
                 <div class="text-gray-900 dark:text-white font-bold leading-tight group-hover:text-blue-600 transition-colors truncate pr-4">{{ product.name }}</div>
                 <Button :icon="product.favorite ? 'pi pi-star-fill' : 'pi pi-star'" 
                         :severity="product.favorite ? 'warn' : 'secondary'" 
                         text rounded size="small" 
                         class="!w-6 !h-6 !p-0 -mt-1 -mr-1 transition-transform hover:scale-125"
                         @click="toggleFavorite(product, $event)" />
               </div>
               <div class="text-[10px] text-gray-400 dark:text-gray-500 font-medium">Stock: {{ product.stock }}</div>
            </div>
            
            <div class="flex justify-between items-end mt-4">
               <div class="text-xl font-black text-gray-900 dark:text-white">${{ product.price.toFixed(2) }}</div>
               <div class="p-2 bg-blue-50 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 rounded-xl group-hover:bg-blue-600 group-hover:text-white transition-all">
                  <i class="pi pi-plus text-xs" />
               </div>
            </div>
          </div>

          <div v-if="product.stock <= 5" class="absolute top-0 right-0">
             <div class="bg-red-500 text-white text-[8px] font-bold px-1.5 py-0.5 rounded-bl-lg uppercase">Bajo</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Carrito -->
    <div class="w-80 md:w-96 bg-white dark:bg-gray-900 shadow-2xl flex flex-col border-l border-gray-100 dark:border-gray-800 transition-colors duration-300">
      <div class="p-6 border-b border-gray-50 dark:border-gray-800 flex justify-between items-center">
        <div>
          <span class="text-xl font-black text-gray-800 dark:text-white">Orden Actual</span>
          <p class="text-xs text-gray-400">{{ cartStore.items.length }} productos</p>
        </div>
        <Button icon="pi pi-trash" severity="danger" text rounded @click="cartStore.clear()" :disabled="cartStore.items.length === 0" />
      </div>

      <div class="flex-1 overflow-y-auto px-6 py-4 space-y-4">
        <div v-if="cartStore.items.length === 0" class="h-full flex flex-col items-center justify-center text-gray-300 dark:text-gray-700 opacity-60">
          <i class="pi pi-shopping-cart text-5xl mb-4" />
          <p class="font-bold">Carrito vacío</p>
        </div>
        
        <div v-for="item in cartStore.items" :key="item.id" 
             class="flex justify-between items-center bg-gray-50 dark:bg-gray-950 p-4 rounded-2xl group transition-all border border-transparent hover:border-gray-200 dark:hover:border-gray-800">
          <div class="flex-1">
            <div class="font-bold text-sm text-gray-800 dark:text-gray-200">{{ item.name }}</div>
            <div class="text-xs text-blue-600 dark:text-blue-400 font-bold mt-1">${{ (item.price * item.quantity).toFixed(2) }}</div>
          </div>
          <div class="flex items-center gap-2 bg-white dark:bg-gray-900 p-1 rounded-xl shadow-sm ring-1 ring-gray-100 dark:ring-gray-800">
            <Button icon="pi pi-minus" class="w-6 h-6 p-0" severity="secondary" text rounded @click="cartStore.removeItem(item.id)" />
            <span class="font-bold text-xs min-w-[1rem] text-center dark:text-gray-300">{{ item.quantity }}</span>
            <Button icon="pi pi-plus" class="w-6 h-6 p-0" severity="secondary" text rounded @click="cartStore.addItem(item)" />
          </div>
        </div>
      </div>

      <div class="p-6 bg-gray-50 dark:bg-gray-950 border-t border-gray-100 dark:border-gray-800">
        <div class="flex justify-between items-center mb-6">
          <span class="text-gray-500 dark:text-gray-400 font-medium">Total</span>
          <span class="text-4xl font-black text-gray-900 dark:text-white tracking-tighter">${{ cartStore.total.toFixed(2) }}</span>
        </div>
        
        <Button label="COBRAR" 
                icon="pi pi-arrow-right" 
                iconPos="right"
                class="w-full h-14 text-lg font-black rounded-2xl shadow-lg bg-blue-600 border-0 hover:bg-blue-700 transition-all active:scale-[0.98]" 
                :disabled="cartStore.items.length === 0 || !activeSession"
                @click="showPaymentDialog = true" />
      </div>
    </div>

    <!-- Pago Dialog -->
    <Dialog v-model:visible="showPaymentDialog" modal header="Finalizar Venta" :style="{ width: '400px' }" class="p-fluid dark:bg-gray-900">
      <div v-if="saleSuccess" class="text-center py-10">
        <i class="pi pi-check-circle text-6xl text-green-500 mb-4" />
        <h2 class="text-2xl font-black text-gray-800 dark:text-white">¡Venta Exitosa!</h2>
      </div>
      
      <div v-else class="space-y-6 pt-4">
        <div>
          <label class="font-bold text-gray-700 dark:text-gray-300 block mb-3">Método de Pago</label>
          <div class="grid grid-cols-2 gap-3">
            <div @click="paymentMethod = 'CASH'" 
                 :class="['cursor-pointer p-4 rounded-2xl border-2 transition-all flex flex-col items-center gap-2', 
                          paymentMethod === 'CASH' ? 'border-blue-600 bg-blue-50 dark:bg-blue-900/30 text-blue-700 dark:text-blue-400' : 'border-gray-100 dark:border-gray-800 bg-white dark:bg-gray-900 text-gray-400']">
              <i class="pi pi-money-bill text-2xl" />
              <span class="font-bold text-[10px] uppercase">Efectivo</span>
            </div>
            <div @click="paymentMethod = 'DEUNA_TRANSFER'" 
                 :class="['cursor-pointer p-4 rounded-2xl border-2 transition-all flex flex-col items-center gap-2', 
                          paymentMethod === 'DEUNA_TRANSFER' ? 'border-purple-600 bg-purple-50 dark:bg-purple-900/30 text-purple-700 dark:text-purple-400' : 'border-gray-100 dark:border-gray-800 bg-white dark:bg-gray-900 text-gray-400']">
              <i class="pi pi-qrcode text-2xl" />
              <span class="font-bold text-[10px] uppercase">DEUNA</span>
            </div>
          </div>
        </div>

        <div v-if="paymentMethod === 'DEUNA_TRANSFER'" class="space-y-2">
          <label class="font-bold text-gray-700 dark:text-gray-300">Referencia / Comprobante</label>
          <InputText v-model="referenceCode" placeholder="Ej. 998877" class="dark:bg-gray-800 dark:text-white" />
        </div>

        <div class="bg-gray-900 text-white p-6 rounded-2xl flex justify-between items-center">
           <span class="text-gray-400 text-sm">Total</span>
           <span class="text-3xl font-black">${{ cartStore.total.toFixed(2) }}</span>
        </div>

        <div class="flex gap-3">
          <Button label="Cancelar" text @click="showPaymentDialog = false" class="flex-1 dark:text-gray-400" />
          <Button label="Finalizar" 
                  :disabled="paymentMethod === 'DEUNA_TRANSFER' && !referenceCode"
                  :loading="loading" 
                  @click="handleProcessPayment" 
                  class="flex-1 !h-12 !font-bold" />
        </div>
      </div>
    </Dialog>
  </div>
</template>

<style scoped>
::-webkit-scrollbar {
  width: 4px;
}
::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 10px;
}
.dark ::-webkit-scrollbar-thumb {
  background: #334155;
}
</style>

<style scoped>
::-webkit-scrollbar {
  width: 4px;
}
::-webkit-scrollbar-thumb {
  background: #e2e8f0;
  border-radius: 10px;
}
:deep(.p-dialog-header) {
  padding-bottom: 0;
}
:deep(.p-message-text) {
  font-size: 0.85rem;
}
</style>
