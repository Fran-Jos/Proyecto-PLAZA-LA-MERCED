<script setup>
import { ref, onMounted, computed } from 'vue'
import { ProductService, SaleService } from '../service/api'
import { useCartStore } from '../store/cart'
import { useAuthStore } from '../store/auth'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Card from 'primevue/card'
import Dialog from 'primevue/dialog'
import Message from 'primevue/message'
import { emitDataChanged, DataEvents } from '../utils/realtimeEvents'

const products = ref([])
const categories = ref([])
const searchQuery = ref('')
const showPaymentDialog = ref(false)
const paymentMethod = ref('CASH')
const referenceCode = ref('')
const loading = ref(false)
const saleSuccess = ref(false)

const cartStore = useCartStore()
const authStore = useAuthStore()

onMounted(async () => {
  try {
    const [prodRes, catRes] = await Promise.all([
      ProductService.getAll(),
      ProductService.getCategories()
    ])
    products.value = prodRes.data
    categories.value = catRes.data
  } catch (err) {
    console.error('Error al cargar datos', err)
  }
})

const filteredProducts = computed(() => {
  if (!searchQuery.value) return products.value
  return products.value.filter(p => 
    p.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
    p.barcode?.includes(searchQuery.value)
  )
})

const handleProcessPayment = async () => {
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
    saleSuccess.value = true
    cartStore.clear()
    setTimeout(() => {
      saleSuccess.value = false
      showPaymentDialog.value = false
    }, 2000)
    // Recargar productos para actualizar stock
    const prodRes = await ProductService.getAll()
    products.value = prodRes.data
  } catch (err) {
    alert(err.response?.data?.message || 'Error al procesar la venta')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="flex h-screen bg-gray-100 overflow-hidden">
    <!-- Area de Productos -->
    <div class="flex-1 flex flex-col p-4 overflow-hidden">
      <div class="mb-4">
        <span class="p-input-icon-left w-full">
          <i class="pi pi-search ml-2" />
          <InputText v-model="searchQuery" placeholder="Buscar producto o código de barras..." class="w-full" />
        </span>
      </div>

      <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 overflow-y-auto">
        <div v-for="product in filteredProducts" :key="product.id" 
             @click="cartStore.addItem(product)"
             class="cursor-pointer transition-transform active:scale-95">
          <Card class="h-full hover:shadow-md border-2 border-transparent hover:border-blue-400">
            <template #title>
              <div class="text-sm font-bold truncate">{{ product.name }}</div>
            </template>
            <template #content>
              <div class="flex justify-between items-center">
                <span class="text-lg font-bold text-green-600">${{ product.price.toFixed(2) }}</span>
                <span :class="['text-xs px-2 py-1 rounded', product.stock > 5 ? 'bg-blue-100 text-blue-700' : 'bg-red-100 text-red-700']">
                  Stock: {{ product.stock }}
                </span>
              </div>
            </template>
          </Card>
        </div>
      </div>
    </div>

    <!-- Carrito / Lateral de Cobro -->
    <div class="w-96 bg-white shadow-xl flex flex-col">
      <div class="p-4 border-b bg-blue-600 text-white flex justify-between items-center">
        <span class="text-xl font-bold">Carrito</span>
        <Button icon="pi pi-trash" severity="danger" text @click="cartStore.clear()" />
      </div>

      <div class="flex-1 overflow-y-auto p-4 space-y-4">
        <div v-if="cartStore.items.length === 0" class="text-center text-gray-400 mt-10">
          <i class="pi pi-shopping-cart text-5xl mb-2" />
          <p>El carrito está vacío</p>
        </div>
        
        <div v-for="item in cartStore.items" :key="item.id" class="flex justify-between items-center bg-gray-50 p-3 rounded-lg">
          <div class="flex-1">
            <div class="font-bold text-sm">{{ item.name }}</div>
            <div class="text-xs text-gray-500">${{ item.price.toFixed(2) }} x {{ item.quantity }}</div>
          </div>
          <div class="flex items-center gap-2">
            <Button icon="pi pi-minus" class="p-button-rounded p-button-sm" severity="secondary" @click="cartStore.removeItem(item.id)" />
            <span class="font-bold w-4 text-center">{{ item.quantity }}</span>
            <Button icon="pi pi-plus" class="p-button-rounded p-button-sm" @click="cartStore.addItem(item)" />
          </div>
        </div>
      </div>

      <div class="p-4 border-t bg-gray-50">
        <div class="flex justify-between items-center mb-4">
          <span class="text-gray-600">Total a pagar:</span>
          <span class="text-3xl font-black text-blue-700">${{ cartStore.total.toFixed(2) }}</span>
        </div>
        <Button label="COBRAR AHORA" icon="pi pi-check-circle" class="w-full h-16 text-xl" 
                :disabled="cartStore.items.length === 0"
                @click="showPaymentDialog = true" />
      </div>
    </div>

    <!-- Diálogo de Pago -->
    <Dialog v-model:visible="showPaymentDialog" modal header="Procesar Pago" :style="{ width: '400px' }">
      <div v-if="saleSuccess" class="text-center p-4">
        <i class="pi pi-check-circle text-6xl text-green-500 mb-4" />
        <h2 class="text-2xl font-bold">¡Venta Exitosa!</h2>
      </div>
      <div v-else class="space-y-6">
        <div class="flex flex-col gap-4">
          <Button :severity="paymentMethod === 'CASH' ? 'primary' : 'secondary'" 
                  label="EFECTIVO" icon="pi pi-money-bill" class="h-14"
                  @click="paymentMethod = 'CASH'" />
          
          <Button :severity="paymentMethod === 'DEUNA_TRANSFER' ? 'primary' : 'secondary'" 
                  label="DEUNA / QR" icon="pi pi-qrcode" class="h-14"
                  @click="paymentMethod = 'DEUNA_TRANSFER'" />
        </div>

        <div v-if="paymentMethod === 'DEUNA_TRANSFER'" class="space-y-2">
          <label class="font-bold block text-blue-800">Número de Comprobante (Banco)</label>
          <InputText v-model="referenceCode" placeholder="Ingrese el número del voucher" class="w-full border-2 border-blue-200" />
          <Message severity="warn" variant="simple" v-if="!referenceCode">El número es obligatorio para continuar</Message>
        </div>

        <div class="pt-4 border-t">
          <div class="flex justify-between mb-4 italic">
            <span>Monto a verificar:</span>
            <span class="font-bold text-xl">${{ cartStore.total.toFixed(2) }}</span>
          </div>
          <Button label="CONFIRMAR Y FINALIZAR" 
                  class="w-full h-12 text-lg font-bold" 
                  :severity="paymentMethod === 'DEUNA_TRANSFER' && !referenceCode ? 'secondary' : 'primary'"
                  :disabled="paymentMethod === 'DEUNA_TRANSFER' && !referenceCode"
                  :loading="loading" 
                  @click="handleProcessPayment" />
        </div>
      </div>
    </Dialog>
  </div>
</template>

<style scoped>
/* Custom scrollbar para que se vea más limpio */
::-webkit-scrollbar {
  width: 6px;
}
::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 10px;
}
</style>
