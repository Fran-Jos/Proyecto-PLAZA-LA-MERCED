<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { SaleService, CashService } from '../service/api'
import { subscribeDataChanged } from '../utils/realtimeEvents'
import { useAuthStore } from '../store/auth'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Card from 'primevue/card'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import Dialog from 'primevue/dialog'
import InputNumber from 'primevue/inputnumber'
import Message from 'primevue/message'

const authStore = useAuthStore()
const sales = ref([])
const activeSession = ref(null)
const cashHistory = ref([])
const loading = ref(false)
const showCloseDialog = ref(false)
const reportedBalance = ref(0)
let unsubscribeEvents = null

onMounted(() => {
  loadData()
  unsubscribeEvents = subscribeDataChanged(() => loadData(false))
  document.addEventListener('visibilitychange', handleVisibilityRefresh)
})

onUnmounted(() => {
  if (unsubscribeEvents) unsubscribeEvents()
  document.removeEventListener('visibilitychange', handleVisibilityRefresh)
})

const handleVisibilityRefresh = () => {
  if (document.visibilityState === 'visible') loadData(false)
}

const loadData = async (showLoading = true) => {
  if (showLoading) loading.value = true
  try {
    const [salesRes, activeRes, historyRes] = await Promise.all([
      SaleService.getAll(),
      CashService.getActive(),
      CashService.getHistory()
    ])
    sales.value = Array.isArray(salesRes.data) ? salesRes.data : []
    activeSession.value = activeRes.data || null
    cashHistory.value = Array.isArray(historyRes.data) ? historyRes.data : []
  } catch (err) {
    console.error('Error cargando reportes', err)
  } finally {
    if (showLoading) loading.value = false
  }
}

const salesInActiveSession = computed(() => {
  if (!activeSession.value) return []
  const openedAt = new Date(activeSession.value.openedAt)
  return sales.value.filter(s => new Date(s.createdAt) >= openedAt)
})

const totalDaily = computed(() => salesInActiveSession.value.reduce((acc, s) => acc + Number(s.total || 0), 0))

const salesByMethod = computed(() => ({
  cash: salesInActiveSession.value
    .filter(s => s.paymentMethod === 'CASH')
    .reduce((acc, s) => acc + Number(s.total || 0), 0),
  deuna: salesInActiveSession.value
    .filter(s => s.paymentMethod === 'DEUNA_TRANSFER')
    .reduce((acc, s) => acc + Number(s.total || 0), 0)
}))

const cashInBox = computed(() => {
  if (!activeSession.value) return 0
  return Number(activeSession.value.openingBalance || 0) + salesByMethod.value.cash
})

const handleCloseCash = async () => {
  try {
    await CashService.close({ userId: authStore.user.id, balance: reportedBalance.value })
    showCloseDialog.value = false
    reportedBalance.value = 0
    await loadData(false)
    alert('Caja cerrada con éxito. Se agregó al historial de caja.')
  } catch (err) {
    alert(err.response?.data?.message || 'Error al cerrar caja')
  }
}

const formatDate = (dateString) => new Date(dateString).toLocaleString()
const calcDifference = (session) => Number(session.reportedBalance || 0) - Number(session.closingBalance || 0)

const productStats = computed(() => {
  const counter = {}
  salesInActiveSession.value.forEach(sale => {
    (sale.items || []).forEach(item => {
      const name = item.product?.name || `Producto #${item.product?.id || ''}`
      counter[name] = (counter[name] || 0) + Number(item.quantity || 0)
    })
  })
  const entries = Object.entries(counter).sort((a,b) => b[1]-a[1])
  return {
    top: entries.slice(0,5),
    low: entries.slice(-5).reverse(),
    max: entries.length ? entries[0][1] : 1
  }
})


const paymentTotal = computed(() => salesByMethod.value.cash + salesByMethod.value.deuna)
const cashPercent = computed(() => paymentTotal.value ? (salesByMethod.value.cash / paymentTotal.value) * 100 : 0)
const deunaPercent = computed(() => paymentTotal.value ? (salesByMethod.value.deuna / paymentTotal.value) * 100 : 0)
const paymentPieStyle = computed(() => ({
  background: `conic-gradient(#22c55e 0% ${cashPercent.value}%, #a855f7 ${cashPercent.value}% 100%)`
}))

const bestSalesDay = computed(() => {
  const daily = {}
  sales.value.forEach(s => {
    const day = new Date(s.createdAt).toLocaleDateString()
    daily[day] = (daily[day] || 0) + Number(s.total || 0)
  })
  const sorted = Object.entries(daily).sort((a,b) => b[1]-a[1])
  return sorted[0] || null
})

</script>

<template>
  <div class="p-6 bg-gray-50 min-h-screen">
    <div class="max-w-7xl mx-auto space-y-6">
      <div class="flex justify-between items-center">
        <h1 class="text-3xl font-bold text-gray-800">Reportes y Control de Caja</h1>
        <router-link to="/"><Button icon="pi pi-home" label="Inicio" text /></router-link>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card class="bg-blue-600 text-white"><template #title>Ventas Totales Hoy</template><template #content><div class="text-4xl font-black">${{ totalDaily.toFixed(2) }}</div></template></Card>
        <Card><template #title>Efectivo en Caja</template><template #content><div class="text-4xl font-black text-green-600">${{ cashInBox.toFixed(2) }}</div></template></Card>
        <Card><template #title>Transferencias DEUNA</template><template #content><div class="text-4xl font-black text-purple-600">${{ salesByMethod.deuna.toFixed(2) }}</div></template></Card>
      </div>

      <Card>
        <template #title>Sesión de Caja Actual</template>
        <template #content>
          <div v-if="activeSession" class="flex justify-between items-center">
            <div>
              <p>Abierta el: <b>{{ formatDate(activeSession.openedAt) }}</b></p>
              <p>Monto Inicial: <b>${{ Number(activeSession.openingBalance || 0).toFixed(2) }}</b></p>
              <p>Efectivo Esperado: <b>${{ cashInBox.toFixed(2) }}</b></p>
            </div>
            <Button label="CERRAR CAJA" icon="pi pi-lock" severity="danger" @click="showCloseDialog = true" />
          </div>
          <div v-else class="text-center p-4"><Message severity="warn" variant="simple">No hay una sesión de caja activa. Abre una desde el módulo Caja.</Message></div>
        </template>
      </Card>

      <Card>
        <template #title>Historial de Ventas (Sesión Actual)</template>
        <template #content>
          <DataTable :value="salesInActiveSession" paginator :rows="5" :loading="loading" class="p-datatable-sm">
            <Column field="createdAt" header="Fecha"><template #body="slotProps">{{ formatDate(slotProps.data.createdAt) }}</template></Column>
            <Column field="paymentMethod" header="Método"><template #body="slotProps"><Tag :value="slotProps.data.paymentMethod" :severity="slotProps.data.paymentMethod === 'CASH' ? 'success' : 'help'" /></template></Column>
            <Column field="referenceCode" header="Referencia (DEUNA)"></Column>
            <Column header="Total"><template #body="slotProps"><span class="font-bold">${{ Number(slotProps.data.total || 0).toFixed(2) }}</span></template></Column>
          </DataTable>
        </template>
      </Card>

      <Card>
        <template #title>Historial de Caja</template>
        <template #content>
          <DataTable :value="cashHistory" paginator :rows="5" class="p-datatable-sm">
            <Column field="openedAt" header="Apertura"><template #body="slotProps">{{ formatDate(slotProps.data.openedAt) }}</template></Column>
            <Column field="closedAt" header="Cierre"><template #body="slotProps">{{ slotProps.data.closedAt ? formatDate(slotProps.data.closedAt) : 'En curso' }}</template></Column>
            <Column field="openingBalance" header="Inicial"><template #body="slotProps">${{ Number(slotProps.data.openingBalance || 0).toFixed(2) }}</template></Column>
            <Column field="closingBalance" header="Esperado"><template #body="slotProps">${{ Number(slotProps.data.closingBalance || 0).toFixed(2) }}</template></Column>
            <Column field="reportedBalance" header="Reportado"><template #body="slotProps">${{ Number(slotProps.data.reportedBalance || 0).toFixed(2) }}</template></Column>
            <Column header="Diferencia"><template #body="slotProps"><span :class="calcDifference(slotProps.data) === 0 ? 'text-green-600' : 'text-red-500'">${{ calcDifference(slotProps.data).toFixed(2) }}</span></template></Column>
          </DataTable>
        </template>
      </Card>
    </div>


      <Card>
        <template #title>Dashboard Comercial</template>
        <template #content>
          <div class="grid md:grid-cols-2 gap-6">
            <div>
              <h3 class="font-bold mb-2">Día con más ventas</h3>
              <p v-if="bestSalesDay"><b>{{ bestSalesDay[0] }}</b> - ${{ Number(bestSalesDay[1]).toFixed(2) }}</p>
              <p v-else>Sin datos suficientes.</p>

              <h3 class="font-bold mt-4 mb-2">Distribución por método de pago</h3>
              <div class="flex items-center gap-4 mb-4">
                <div class="w-28 h-28 rounded-full" :style="paymentPieStyle"></div>
                <div class="text-sm">
                  <p><span class="inline-block w-3 h-3 bg-green-500 rounded-full mr-2"></span>Efectivo: ${{ salesByMethod.cash.toFixed(2) }} ({{ cashPercent.toFixed(1) }}%)</p>
                  <p><span class="inline-block w-3 h-3 bg-purple-500 rounded-full mr-2"></span>DEUNA: ${{ salesByMethod.deuna.toFixed(2) }} ({{ deunaPercent.toFixed(1) }}%)</p>
                </div>
              </div>

              <h3 class="font-bold mt-4 mb-2">Productos más vendidos</h3>
              <div v-for="item in productStats.top" :key="item[0]" class="mb-2">
                <div class="text-sm">{{ item[0] }} ({{ item[1] }})</div>
                <div class="h-2 bg-gray-200 rounded"><div class="h-2 bg-green-500 rounded" :style="{width: ((item[1]/productStats.max)*100)+'%'}"></div></div>
              </div>
            </div>
            <div>
              <h3 class="font-bold mb-2">Productos con baja rotación</h3>
              <div v-for="item in productStats.low" :key="item[0]" class="mb-2">
                <div class="text-sm">{{ item[0] }} ({{ item[1] }})</div>
                <div class="h-2 bg-gray-200 rounded"><div class="h-2 bg-purple-500 rounded" :style="{width: ((item[1]/productStats.max)*100)+'%'}"></div></div>
              </div>
            </div>
          </div>
        </template>
      </Card>

    <Dialog v-model:visible="showCloseDialog" header="Cierre de Caja (Arqueo Ciego)" modal :style="{ width: '400px' }">
      <div class="space-y-4 pt-4">
        <p>Por favor, cuente el dinero físico en caja e ingrese el total:</p>
        <label class="font-bold block mb-1">Efectivo Total Contado</label>
        <InputNumber v-model="reportedBalance" mode="currency" currency="USD" locale="en-US" class="w-full" autofocus />
      </div>
      <template #footer>
        <Button label="Cancelar" icon="pi pi-times" text @click="showCloseDialog = false" />
        <Button label="Finalizar Cierre" icon="pi pi-check" severity="danger" @click="handleCloseCash" />
      </template>
    </Dialog>
  </div>
</template>
