<script setup>
import { ref, onMounted, computed } from 'vue'
import { CashService } from '../service/api'
import { useAuthStore } from '../store/auth'
import Card from 'primevue/card'
import InputNumber from 'primevue/inputnumber'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import ThemeToggle from '../components/ThemeToggle.vue'
import { emitDataChanged, DataEvents } from '../utils/realtimeEvents'

const authStore = useAuthStore()
const activeSession = ref(null)
const openingBalance = ref(0)
const boxName = ref('')
const closingBalance = ref(0)
const loading = ref(false)
const error = ref('')

const canOpen = computed(() => !activeSession.value)

const loadActive = async () => {
  try {
    const response = await CashService.getActive()
    activeSession.value = response.data || null
  } catch {
    activeSession.value = null
  }
}

const openCash = async () => {
  if (!boxName.value || openingBalance.value <= 0) {
    error.value = 'Ingrese nombre de caja y un monto válido.'
    return
  }

  loading.value = true
  error.value = ''
  try {
    await CashService.open({ 
      userId: authStore.user.id, 
      balance: openingBalance.value, 
      boxName: boxName.value 
    })
    emitDataChanged(DataEvents.CASH_OPENED)
    await loadActive()
    boxName.value = ''
    openingBalance.value = 0
  } catch (err) {
    error.value = err.response?.data?.message || 'No se pudo abrir caja'
  } finally {
    loading.value = false
  }
}

const closeCash = async () => {
  loading.value = true
  error.value = ''
  try {
    await CashService.close({ userId: authStore.user.id, balance: closingBalance.value || 0 })
    emitDataChanged(DataEvents.CASH_CLOSED)
    activeSession.value = null
    closingBalance.value = 0
    alert('Caja cerrada con éxito. Los resultados se ven en Reportes.')
  } catch (err) {
    error.value = err.response?.data?.message || 'No se pudo cerrar caja'
  } finally {
    loading.value = false
  }
}

onMounted(loadActive)

const formatCurrency = (value) => {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value || 0)
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-950 p-4 md:p-10 font-sans text-gray-900 dark:text-gray-100 transition-colors duration-300">
    <div class="max-w-3xl mx-auto">
      
      <header class="mb-10 flex flex-col items-center">
        <div class="flex justify-between w-full items-center mb-6">
           <router-link to="/"><Button icon="pi pi-arrow-left" text rounded severity="secondary" /></router-link>
           <ThemeToggle />
        </div>
        <h1 class="text-4xl font-black tracking-tight text-gray-900 dark:text-white">Control de Caja</h1>
        <p class="text-gray-500 dark:text-gray-400 mt-2">Gestión de aperturas y cierres de terminales.</p>
      </header>

      <!-- Pantalla de Apertura -->
      <div v-if="canOpen" class="bg-white dark:bg-gray-900 rounded-3xl shadow-xl border border-gray-100 dark:border-gray-800 overflow-hidden animate-in fade-in zoom-in duration-300">
        <div class="p-8 border-b border-gray-50 dark:border-gray-800 bg-gradient-to-r from-blue-600 to-blue-700 text-white">
          <div class="flex items-center gap-4">
            <div class="p-3 bg-white/20 rounded-2xl"><i class="pi pi-lock-open text-2xl"></i></div>
            <div>
              <h2 class="text-xl font-bold">Nueva Apertura</h2>
              <p class="text-blue-100 text-sm">Inicie una sesión para comenzar a vender.</p>
            </div>
          </div>
        </div>

        <div class="p-8 space-y-6">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div class="space-y-2">
              <label class="text-xs font-bold text-gray-700 dark:text-gray-400 uppercase tracking-wider">Nombre de Caja</label>
              <InputText v-model="boxName" placeholder="Ej. Caja Principal" 
                         class="w-full h-14 rounded-2xl border border-gray-100 dark:border-gray-800 bg-gray-50 dark:bg-gray-800 focus:bg-white dark:focus:bg-gray-950 transition-all text-lg font-bold dark:text-white" />
            </div>
            <div class="space-y-2">
              <label class="text-xs font-bold text-gray-700 dark:text-gray-400 uppercase tracking-wider">Monto Inicial</label>
              <InputNumber v-model="openingBalance" mode="currency" currency="USD" locale="en-US" 
                           class="w-full h-14" inputClass="rounded-2xl border border-gray-100 dark:border-gray-800 bg-gray-50 dark:bg-gray-800 focus:bg-white dark:focus:bg-gray-950 text-lg font-black text-green-600 dark:text-green-400" />
            </div>
          </div>

          <Message v-if="error" severity="error" variant="simple" class="rounded-xl">{{ error }}</Message>

          <Button label="CONFIRMAR APERTURA" 
                  icon="pi pi-check" 
                  class="w-full h-16 text-lg font-black rounded-2xl bg-blue-600 hover:bg-blue-700 border-0 shadow-lg shadow-blue-200 dark:shadow-none" 
                  :loading="loading" 
                  @click="openCash" />
        </div>
      </div>

      <!-- Pantalla de Sesión Activa -->
      <div v-else class="bg-white dark:bg-gray-900 rounded-3xl shadow-xl border border-gray-100 dark:border-gray-800 overflow-hidden animate-in fade-in slide-in-from-bottom duration-300">
        <div class="p-8 border-b border-gray-50 dark:border-gray-800 bg-gradient-to-r from-emerald-600 to-emerald-700 text-white flex justify-between items-center">
          <div class="flex items-center gap-4">
            <div class="p-3 bg-white/20 rounded-2xl"><i class="pi pi-unlock text-2xl"></i></div>
            <div>
              <h2 class="text-xl font-bold">Sesión en Curso</h2>
              <p class="text-emerald-100 text-sm">Terminal: {{ activeSession.name }}</p>
            </div>
          </div>
          <div class="px-3 py-1 bg-emerald-500 rounded-full text-[10px] font-black uppercase tracking-widest animate-pulse">En Línea</div>
        </div>

        <div class="p-8">
          <div class="bg-gray-50 dark:bg-gray-950 rounded-2xl p-6 mb-8 grid grid-cols-2 gap-4 border border-gray-100 dark:border-gray-800">
             <div>
                <p class="text-[10px] text-gray-400 font-black uppercase tracking-widest mb-1">Apertura</p>
                <p class="text-2xl font-black text-gray-800 dark:text-gray-100">{{ formatCurrency(activeSession.openingBalance) }}</p>
             </div>
             <div class="text-right">
                <p class="text-[10px] text-gray-400 font-black uppercase tracking-widest mb-1">Hora Inicio</p>
                <p class="text-sm font-bold text-gray-800 dark:text-gray-100">{{ new Date(activeSession.openedAt).toLocaleTimeString() }}</p>
             </div>
          </div>

          <div class="space-y-6 text-center">
             <h3 class="font-bold text-gray-800 dark:text-gray-200 mb-2">Cierre de Caja</h3>
             <p class="text-xs text-gray-400 mb-6">Realice el arqueo contando el dinero físico.</p>
             
             <div class="max-w-xs mx-auto space-y-2 mb-8">
               <label class="text-[10px] font-black text-gray-500 uppercase tracking-widest">Efectivo Total Contado</label>
               <InputNumber v-model="closingBalance" mode="currency" currency="USD" locale="en-US" 
                            class="w-full h-16" inputClass="rounded-2xl text-2xl font-black text-center border border-gray-100 dark:border-gray-800 bg-gray-50 dark:bg-gray-800 dark:text-white focus:border-blue-500" />
             </div>

             <Message v-if="error" severity="error" variant="simple" class="rounded-xl mb-4">{{ error }}</Message>

             <div class="flex flex-col sm:flex-row gap-4">
               <router-link to="/pos" class="flex-1">
                  <Button label="Vender" icon="pi pi-shopping-cart" class="w-full h-14 rounded-2xl bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300 border-0 font-bold" />
               </router-link>
               <Button label="FINALIZAR SESIÓN" 
                       severity="danger" 
                       icon="pi pi-lock" 
                       class="flex-[2] h-14 font-black rounded-2xl bg-red-600 hover:bg-red-700 border-0 shadow-lg shadow-red-100 dark:shadow-none" 
                       :loading="loading" 
                       @click="closeCash" />
             </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.animate-pulse {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: .5; }
}
</style>
