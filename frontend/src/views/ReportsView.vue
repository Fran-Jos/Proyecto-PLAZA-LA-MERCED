<script setup>
import { ref, onMounted, computed } from 'vue'
import { SaleService, CashService } from '../service/api'
import { useAuthStore } from '../store/auth'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Card from 'primevue/card'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import Dialog from 'primevue/dialog'
import InputNumber from 'primevue/inputnumber'

const authStore = useAuthStore()
const sales = ref([])
const activeSession = ref(null)
const loading = ref(false)
const showCloseDialog = ref(false)
const reportedBalance = ref(0)

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const [salesRes, cashRes] = await Promise.all([
      SaleService.getAll(),
      CashService.getActive(authStore.user.id)
    ])
    sales.value = salesRes.data
    activeSession.value = cashRes.data || null
  } finally {
    loading.value = false
  }
}

const totalDaily = computed(() => {
  const today = new Date().toLocaleDateString()
  return sales.value
    .filter(s => new Date(s.createdAt).toLocaleDateString() === today)
    .reduce((acc, s) => acc + s.total, 0)
})

const salesByMethod = computed(() => {
  const today = new Date().toLocaleDateString()
  const todaySales = sales.value.filter(s => new Date(s.createdAt).toLocaleDateString() === today)
  
  return {
    cash: todaySales.filter(s => s.paymentMethod === 'CASH').reduce((acc, s) => acc + s.total, 0),
    deuna: todaySales.filter(s => s.paymentMethod === 'DEUNA_TRANSFER').reduce((acc, s) => acc + s.total, 0)
  }
})

const handleCloseCash = async () => {
  try {
    await CashService.close({
      userId: authStore.user.id,
      balance: reportedBalance.value
    })
    showCloseDialog.value = false
    loadData()
    alert('Caja cerrada con éxito. Revisa el descuadre en el historial.')
  } catch (err) {
    alert('Error al cerrar caja')
  }
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleString()
}
</script>

<template>
  <div class="p-6 bg-gray-50 min-h-screen">
    <div class="max-w-7xl mx-auto space-y-6">
      
      <div class="flex justify-between items-center">
        <h1 class="text-3xl font-bold text-gray-800">Reportes y Control de Caja</h1>
        <router-link to="/">
          <Button icon="pi pi-home" label="Inicio" text />
        </router-link>
      </div>

      <!-- Resumen del Día -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card class="bg-blue-600 text-white">
          <template #title>Ventas Totales Hoy</template>
          <template #content>
            <div class="text-4xl font-black">${{ totalDaily.toFixed(2) }}</div>
          </template>
        </Card>
        <Card>
          <template #title>Efectivo en Caja</template>
          <template #content>
            <div class="text-4xl font-black text-green-600">${{ salesByMethod.cash.toFixed(2) }}</div>
          </template>
        </Card>
        <Card>
          <template #title>Transferencias DEUNA</template>
          <template #content>
            <div class="text-4xl font-black text-purple-600">${{ salesByMethod.deuna.toFixed(2) }}</div>
          </template>
        </Card>
      </div>

      <!-- Estado de Caja -->
      <Card>
        <template #title>Sesión de Caja Actual</template>
        <template #content>
          <div v-if="activeSession" class="flex justify-between items-center">
            <div>
              <p class="text-gray-600">Abierta el: <b>{{ formatDate(activeSession.openedAt) }}</b></p>
              <p class="text-gray-600">Monto Inicial: <b>${{ activeSession.openingBalance.toFixed(2) }}</b></p>
            </div>
            <Button label="CERRAR CAJA (ARQUEO)" icon="pi pi-lock" severity="danger" @click="showCloseDialog = true" />
          </div>
          <div v-else class="text-center p-4">
            <Message severity="warn" variant="simple">No hay una sesión de caja activa. Abre una desde el POS.</Message>
          </div>
        </template>
      </Card>

      <!-- Historial de Ventas -->
      <Card>
        <template #title>Historial de Ventas Recientes</template>
        <template #content>
          <DataTable :value="sales" paginator :rows="5" class="p-datatable-sm">
            <Column field="createdAt" header="Fecha">
              <template #body="slotProps">
                {{ formatDate(slotProps.data.createdAt) }}
              </template>
            </Column>
            <Column field="paymentMethod" header="Método">
              <template #body="slotProps">
                <Tag :value="slotProps.data.paymentMethod" 
                     :severity="slotProps.data.paymentMethod === 'CASH' ? 'success' : 'help'" />
              </template>
            </Column>
            <Column field="referenceCode" header="Referencia (DEUNA)"></Column>
            <Column header="Total">
              <template #body="slotProps">
                <span class="font-bold">${{ slotProps.data.total.toFixed(2) }}</span>
              </template>
            </Column>
          </DataTable>
        </template>
      </Card>
    </div>

    <!-- Diálogo de Arqueo -->
    <Dialog v-model:visible="showCloseDialog" header="Cierre de Caja (Arqueo Ciego)" modal :style="{ width: '400px' }">
      <div class="space-y-4 pt-4">
        <p class="text-gray-600">Por favor, cuente el dinero físico en caja e ingrese el total:</p>
        <div class="field">
          <label class="font-bold block mb-1">Efectivo Total Contado</label>
          <InputNumber v-model="reportedBalance" mode="currency" currency="USD" locale="en-US" class="w-full" autofocus />
        </div>
        <Message severity="info" variant="simple">El sistema comparará este monto con las ventas registradas para detectar faltantes o sobrantes.</Message>
      </div>
      <template #footer>
        <Button label="Cancelar" icon="pi pi-times" text @click="showCloseDialog = false" />
        <Button label="Finalizar Cierre" icon="pi pi-check" severity="danger" @click="handleCloseCash" />
      </template>
    </Dialog>
  </div>
</template>
