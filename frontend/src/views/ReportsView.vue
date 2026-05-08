<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { ReportService, CashService, SaleService } from '../service/api'
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
import Chart from 'primevue/chart'
import ThemeToggle from '../components/ThemeToggle.vue'

const authStore = useAuthStore()
const dashboard = ref({
  totalSalesToday: 0,
  cashInBox: 0,
  deunaSalesToday: 0,
  dailySales: [],
  topProducts: [],
  leastProducts: [],
  salesByCategory: [],
  totalInventoryValue: 0,
  lowStockCount: 0,
  totalSalesCount: 0,
  activeSession: null
})
const cashHistory = ref([])
const sales = ref([])
const loading = ref(false)
const showCloseDialog = ref(false)
const reportedBalance = ref(0)
let unsubscribeEvents = null

const isDarkMode = ref(document.documentElement.classList.contains('dark'))

onMounted(() => {
  loadData()
  unsubscribeEvents = subscribeDataChanged(() => loadData(false))
  document.addEventListener('visibilitychange', handleVisibilityRefresh)
  
  // Observe theme changes
  const observer = new MutationObserver(() => {
    isDarkMode.value = document.documentElement.classList.contains('dark')
  })
  observer.observe(document.documentElement, { attributes: true, attributeFilter: ['class'] })
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
    const [dashRes, historyRes, salesRes] = await Promise.all([
      ReportService.getDashboard(),
      CashService.getHistory(),
      SaleService.getAll()
    ])
    dashboard.value = dashRes.data
    cashHistory.value = Array.isArray(historyRes.data) ? historyRes.data : []
    sales.value = Array.isArray(salesRes.data) ? salesRes.data : []
  } catch (err) {
    console.error('Error cargando reportes', err)
  } finally {
    if (showLoading) loading.value = false
  }
}

const handleCloseCash = async () => {
  try {
    await CashService.close({ userId: authStore.user.id, balance: reportedBalance.value })
    showCloseDialog.value = false
    reportedBalance.value = 0
    await loadData(false)
    alert('Caja cerrada con éxito.')
  } catch (err) {
    alert(err.response?.data?.message || 'Error al cerrar caja')
  }
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('es-EC', { 
    year: 'numeric', month: 'short', day: 'numeric', 
    hour: '2-digit', minute: '2-digit' 
  })
}

const formatCurrency = (value) => {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value || 0)
}

const calcDifference = (session) => Number(session.reportedBalance || 0) - Number(session.closingBalance || 0)

const salesInActiveSession = computed(() => {
  if (!dashboard.value.activeSession) return []
  return sales.value.filter(s => s.cashSession && s.cashSession.id === dashboard.value.activeSession.id)
})

// Chart Data
const salesChartData = computed(() => {
  const sortedSales = [...dashboard.value.dailySales].reverse()
  return {
    labels: sortedSales.map(d => new Date(d.date).toLocaleDateString('es-EC', { weekday: 'short', day: 'numeric' })),
    datasets: [
      {
        label: 'Ventas Diarias',
        data: sortedSales.map(d => d.total),
        fill: true,
        borderColor: '#3b82f6',
        backgroundColor: 'rgba(59, 130, 246, 0.1)',
        tension: 0.4
      }
    ]
  }
})

const categoryChartData = computed(() => {
  return {
    labels: dashboard.value.salesByCategory.map(c => c.name),
    datasets: [
      {
        data: dashboard.value.salesByCategory.map(c => c.total),
        backgroundColor: ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899'],
        hoverBackgroundColor: ['#2563eb', '#059669', '#d97706', '#dc2626', '#7c3aed', '#db2777']
      }
    ]
  }
})

const chartOptions = computed(() => ({
  plugins: {
    legend: {
      labels: {
        color: isDarkMode.value ? '#e5e7eb' : '#4b5563'
      }
    }
  },
  scales: {
    x: {
      ticks: { color: isDarkMode.value ? '#9ca3af' : '#6b7280' },
      grid: { color: isDarkMode.value ? 'rgba(255, 255, 255, 0.05)' : 'rgba(0, 0, 0, 0.05)' }
    },
    y: {
      ticks: { color: isDarkMode.value ? '#9ca3af' : '#6b7280' },
      grid: { color: isDarkMode.value ? 'rgba(255, 255, 255, 0.05)' : 'rgba(0, 0, 0, 0.05)' }
    }
  }
}))

const pieOptions = computed(() => ({
  plugins: {
    legend: {
      position: 'bottom',
      labels: { color: isDarkMode.value ? '#e5e7eb' : '#4b5563' }
    }
  }
}))
</script>

<template>
  <div class="p-4 md:p-8 bg-gray-50 dark:bg-gray-950 min-h-screen font-sans text-gray-900 dark:text-gray-100 transition-colors duration-300">
    <div class="max-w-7xl mx-auto space-y-8">
      
      <!-- Header -->
      <header class="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 class="text-3xl font-extrabold tracking-tight text-gray-900 dark:text-white">Panel de Control</h1>
          <p class="text-gray-500 dark:text-gray-400 mt-1">Resumen de operaciones y rendimiento comercial.</p>
        </div>
        <div class="flex items-center gap-2">
          <ThemeToggle />
          <Button icon="pi pi-refresh" @click="loadData" text rounded :loading="loading" class="dark:text-gray-300" />
          <router-link to="/"><Button icon="pi pi-arrow-left" label="Volver" severity="secondary" text class="dark:text-gray-300" /></router-link>
        </div>
      </header>

      <!-- KPI Cards -->
      <section class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
        <div class="bg-white dark:bg-gray-900 p-6 rounded-2xl shadow-sm border border-gray-100 dark:border-gray-800 flex flex-col justify-between">
          <div class="flex items-center justify-between mb-2">
            <span class="text-sm font-medium text-gray-500 dark:text-gray-400 uppercase">Ventas Totales</span>
            <div class="p-2 bg-blue-50 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 rounded-lg"><i class="pi pi-shopping-cart"></i></div>
          </div>
          <div class="text-2xl font-bold dark:text-white">{{ formatCurrency(dashboard.totalSalesToday) }}</div>
          <p class="text-xs text-gray-400 mt-2">{{ dashboard.totalSalesCount }} transacciones</p>
        </div>
        
        <div class="bg-white dark:bg-gray-900 p-6 rounded-2xl shadow-sm border border-gray-100 dark:border-gray-800 flex flex-col justify-between">
          <div class="flex items-center justify-between mb-2">
            <span class="text-sm font-medium text-gray-500 dark:text-gray-400 uppercase">Efectivo en Caja</span>
            <div class="p-2 bg-green-50 dark:bg-green-900/30 text-green-600 dark:text-green-400 rounded-lg"><i class="pi pi-dollar"></i></div>
          </div>
          <div class="text-2xl font-bold dark:text-white">{{ formatCurrency(dashboard.cashInBox) }}</div>
          <p class="text-xs text-gray-400 mt-2" v-if="dashboard.activeSession">Incluye base: {{ formatCurrency(dashboard.activeSession.openingBalance) }}</p>
        </div>

        <div class="bg-white dark:bg-gray-900 p-6 rounded-2xl shadow-sm border border-gray-100 dark:border-gray-800 flex flex-col justify-between">
          <div class="flex items-center justify-between mb-2">
            <span class="text-sm font-medium text-gray-500 dark:text-gray-400 uppercase">Ventas Deuna</span>
            <div class="p-2 bg-purple-50 dark:bg-purple-900/30 text-purple-600 dark:text-purple-400 rounded-lg"><i class="pi pi-mobile"></i></div>
          </div>
          <div class="text-2xl font-bold dark:text-white">{{ formatCurrency(dashboard.deunaSalesToday) }}</div>
          <p class="text-xs text-gray-400 mt-2">Transferencias digitales</p>
        </div>

        <div class="bg-white dark:bg-gray-900 p-6 rounded-2xl shadow-sm border border-gray-100 dark:border-gray-800 flex flex-col justify-between">
          <div class="flex items-center justify-between mb-2">
            <span class="text-sm font-medium text-gray-500 dark:text-gray-400 uppercase">Valor Inventario</span>
            <div class="p-2 bg-amber-50 dark:bg-amber-900/30 text-amber-600 dark:text-amber-400 rounded-lg"><i class="pi pi-box"></i></div>
          </div>
          <div class="text-2xl font-bold dark:text-white">{{ formatCurrency(dashboard.totalInventoryValue) }}</div>
          <p class="text-xs text-red-500 font-bold mt-2" v-if="dashboard.lowStockCount > 0">{{ dashboard.lowStockCount }} productos bajos</p>
        </div>

        <div class="bg-white dark:bg-gray-900 p-6 rounded-2xl shadow-sm border border-gray-100 dark:border-gray-800 flex flex-col justify-between">
          <div class="flex items-center justify-between mb-2">
            <span class="text-sm font-medium text-gray-500 dark:text-gray-400 uppercase">Estado Caja</span>
            <div :class="`p-2 rounded-lg ${dashboard.activeSession ? 'bg-emerald-50 dark:bg-emerald-900/30 text-emerald-600' : 'bg-red-50 dark:bg-red-900/30 text-red-600'}`">
              <i :class="`pi ${dashboard.activeSession ? 'pi-unlock' : 'pi-lock'}`"></i>
            </div>
          </div>
          <div class="flex items-center gap-2">
             <div :class="`w-2 h-2 rounded-full ${dashboard.activeSession ? 'bg-emerald-500' : 'bg-red-500'}`"></div>
             <span class="font-bold dark:text-white text-lg">{{ dashboard.activeSession ? 'Abierta' : 'Cerrada' }}</span>
          </div>
          <p class="text-xs text-gray-400 mt-2" v-if="dashboard.activeSession">Caja: {{ dashboard.activeSession.name }}</p>
        </div>
      </section>

      <!-- Charts Section -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <div class="bg-white dark:bg-gray-900 p-6 rounded-2xl shadow-sm border border-gray-100 dark:border-gray-800">
          <h3 class="text-lg font-bold mb-6 dark:text-white">Tendencia de Ventas (7 días)</h3>
          <Chart type="line" :data="salesChartData" :options="chartOptions" class="h-64" />
        </div>
        <div class="bg-white dark:bg-gray-900 p-6 rounded-2xl shadow-sm border border-gray-100 dark:border-gray-800">
          <h3 class="text-lg font-bold mb-6 dark:text-white">Distribución por Categoría</h3>
          <div class="flex justify-center h-64">
            <Chart type="doughnut" :data="categoryChartData" :options="pieOptions" class="w-full max-w-xs" />
          </div>
        </div>
      </div>

      <!-- Main Content Grid -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        <!-- Active Session & Historical -->
        <div class="lg:col-span-2 space-y-8">
          
          <div class="bg-white dark:bg-gray-900 rounded-2xl shadow-sm border border-gray-100 dark:border-gray-800 overflow-hidden">
            <div class="p-6 border-b border-gray-50 dark:border-gray-800 flex justify-between items-center">
               <h3 class="text-lg font-bold dark:text-white">Sesión Actual</h3>
               <Button v-if="dashboard.activeSession" label="Cerrar Caja" severity="danger" @click="showCloseDialog = true" size="small" rounded />
            </div>
            <div v-if="dashboard.activeSession" class="p-0">
               <div class="grid grid-cols-2 md:grid-cols-5 divide-x divide-gray-50 dark:divide-gray-800 text-center">
                  <div class="p-4">
                    <p class="text-xs text-gray-400 uppercase">Apertura</p>
                    <p class="font-bold text-sm dark:text-gray-200">{{ formatDate(dashboard.activeSession.openedAt) }}</p>
                  </div>
                  <div class="p-4">
                    <p class="text-xs text-gray-400 uppercase">Base</p>
                    <p class="font-bold text-sm text-blue-600">{{ formatCurrency(dashboard.activeSession.openingBalance) }}</p>
                  </div>
                  <div class="p-4">
                    <p class="text-xs text-gray-400 uppercase">Ventas Efec.</p>
                    <p class="font-bold text-sm text-green-600">{{ formatCurrency(dashboard.activeSession.cashSales) }}</p>
                  </div>
                  <div class="p-4">
                    <p class="text-xs text-gray-400 uppercase">Ventas Deuna</p>
                    <p class="font-bold text-sm text-purple-600">{{ formatCurrency(dashboard.activeSession.deunaSales) }}</p>
                  </div>
                  <div class="p-4">
                    <p class="text-xs text-gray-400 uppercase">Esperado Efec.</p>
                    <p class="font-bold text-sm text-emerald-700 dark:text-emerald-400">{{ formatCurrency(dashboard.cashInBox) }}</p>
                  </div>
               </div>
               
               <DataTable :value="salesInActiveSession" paginator :rows="5" class="p-datatable-sm" size="small">
                  <Column field="createdAt" header="Hora">
                    <template #body="slotProps">
                      <span class="dark:text-gray-300">{{ new Date(slotProps.data.createdAt).toLocaleTimeString('es-EC', {hour:'2-digit', minute:'2-digit'}) }}</span>
                    </template>
                  </Column>
                  <Column field="paymentMethod" header="Método">
                    <template #body="slotProps">
                      <span :class="`text-[10px] px-2 py-0.5 rounded-full font-bold uppercase ${slotProps.data.paymentMethod === 'CASH' ? 'bg-green-100 text-green-700 dark:bg-green-900/40 dark:text-green-400' : 'bg-purple-100 text-purple-700 dark:bg-purple-900/40 dark:text-purple-400'}`">
                        {{ slotProps.data.paymentMethod === 'CASH' ? 'Efectivo' : 'DEUNA' }}
                      </span>
                    </template>
                  </Column>
                  <Column field="total" header="Total">
                    <template #body="slotProps"><span class="font-semibold dark:text-gray-200">{{ formatCurrency(slotProps.data.total) }}</span></template>
                  </Column>
               </DataTable>
            </div>
            <div v-else class="p-12 text-center text-gray-400">
               <i class="pi pi-info-circle text-4xl mb-4 block"></i>
               <p>No hay una caja abierta.</p>
            </div>
          </div>

          <section class="bg-white dark:bg-gray-900 rounded-2xl shadow-sm border border-gray-100 dark:border-gray-800 overflow-hidden">
            <div class="p-6 border-b border-gray-50 dark:border-gray-800">
              <h3 class="text-lg font-bold dark:text-white">Historial de Cierres</h3>
            </div>
            <DataTable :value="cashHistory" paginator :rows="5" class="p-datatable-sm">
              <Column field="openedAt" header="Apertura">
                <template #body="slotProps"><span class="text-xs dark:text-gray-400">{{ formatDate(slotProps.data.openedAt) }}</span></template>
              </Column>
              <Column header="Diferencia">
                <template #body="slotProps">
                  <span :class="`font-bold text-xs ${calcDifference(slotProps.data) >= 0 ? 'text-green-600' : 'text-red-600'}`">
                    {{ formatCurrency(calcDifference(slotProps.data)) }}
                  </span>
                </template>
              </Column>
              <Column header="Estado">
                <template #body="slotProps">
                  <Tag :value="slotProps.data.status" :severity="slotProps.data.status === 'OPEN' ? 'success' : 'secondary'" rounded />
                </template>
              </Column>
            </DataTable>
          </section>
        </div>

        <!-- Right Side: Performance -->
        <div class="space-y-8">
          <div class="bg-white dark:bg-gray-900 p-6 rounded-2xl shadow-sm border border-gray-100 dark:border-gray-800">
            <h3 class="text-lg font-bold mb-4 dark:text-white">Más Vendidos</h3>
            <div class="space-y-4">
              <div v-for="(item, index) in dashboard.topProducts.slice(0,5)" :key="index" class="space-y-1">
                <div class="flex justify-between text-sm">
                  <span class="font-medium truncate flex-1 pr-2 dark:text-gray-300">{{ item.name }}</span>
                  <span class="text-gray-500">{{ item.totalSold }} ud.</span>
                </div>
                <div class="h-1.5 w-full bg-gray-100 dark:bg-gray-800 rounded-full overflow-hidden">
                  <div class="h-full bg-blue-500" :style="{ width: `${(item.totalSold / (dashboard.topProducts[0]?.totalSold || 1)) * 100}%` }"></div>
                </div>
              </div>
            </div>
          </div>

          <div class="bg-white dark:bg-gray-900 p-6 rounded-2xl shadow-sm border border-gray-100 dark:border-gray-800">
            <h3 class="text-lg font-bold mb-4 text-red-600">Baja Rotación</h3>
            <div class="space-y-3">
              <div v-for="(item, index) in dashboard.leastProducts.slice(0,5)" :key="index" class="flex justify-between items-center text-sm border-b border-gray-50 dark:border-gray-800 pb-2 last:border-0">
                <span class="font-medium text-gray-700 dark:text-gray-400">{{ item.name }}</span>
                <span class="bg-red-50 dark:bg-red-900/30 text-red-600 dark:text-red-400 px-2 py-0.5 rounded text-[10px] font-bold">{{ item.totalSold }} ventas</span>
              </div>
            </div>
          </div>

          <div class="bg-gradient-to-br from-blue-600 to-blue-800 p-6 rounded-3xl shadow-lg text-white">
             <h3 class="text-lg font-bold mb-4">Acceso Rápido</h3>
             <div class="grid grid-cols-1 gap-3">
                <router-link to="/pos"><Button label="Punto de Venta" icon="pi pi-desktop" class="w-full !bg-white/20 !border-0" /></router-link>
                <router-link to="/inventory"><Button label="Gestionar Stock" icon="pi pi-box" class="w-full !bg-white/20 !border-0" /></router-link>
             </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Dialogo de Cierre -->
    <Dialog v-model:visible="showCloseDialog" header="Arqueo de Caja" modal :style="{ width: '400px' }" class="p-fluid dark:bg-gray-900">
      <div class="space-y-4 pt-4">
        <label class="font-bold dark:text-gray-300">Dinero físico en caja (Efectivo)</label>
        <InputNumber v-model="reportedBalance" mode="currency" currency="USD" locale="en-US" class="text-xl" autofocus />
      </div>
      <template #footer>
        <Button label="Cerrar Caja Ahora" severity="danger" @click="handleCloseCash" />
      </template>
    </Dialog>
  </div>
</template>

<style scoped>
:deep(.p-datatable) {
  background: transparent;
}
:deep(.p-datatable .p-datatable-thead > tr > th) {
  background: transparent;
  color: #6b7280;
  font-size: 0.7rem;
  text-transform: uppercase;
}
.dark :deep(.p-datatable .p-datatable-tbody > tr) {
  background: transparent;
  color: #d1d5db;
}
.dark :deep(.p-datatable .p-datatable-tbody > tr:hover) {
  background: rgba(255,255,255,0.02);
}
</style>
