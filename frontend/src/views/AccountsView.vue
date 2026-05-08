<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Dialog from 'primevue/dialog'
import ThemeToggle from '../components/ThemeToggle.vue'
import { ProductService, SaleService, CashService } from '../service/api'
import { useAccountsStore } from '../store/accounts'
import { onMounted } from 'vue'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const accountsStore = useAccountsStore()
const authStore = useAuthStore()
const clientName = ref('')
const search = ref('')
const statusFilter = ref('ALL')
const barcodeQuery = ref('')
const selectedAccountId = ref(null)
const products = ref([])
const showPaymentDialog = ref(false)
const paymentMethod = ref('CASH')
const referenceCode = ref('')
const loading = ref(false)
const activeSession = ref(null)

onMounted(async () => {
  const [prodRes, cashRes] = await Promise.all([ProductService.getAll(), CashService.getActive()])
  products.value = prodRes.data
  activeSession.value = cashRes.data
})

const create = () => {
  if (!clientName.value.trim()) return
  const account = accountsStore.createAccount(clientName.value.trim())
  selectedAccountId.value = account.id
  clientName.value = ''
}

const selectedAccount = computed(() => accountsStore.accounts.find(a => a.id === selectedAccountId.value))
const filteredAccounts = computed(() => accountsStore.accounts.filter(a => {
  const byName = a.clientName.toLowerCase().includes(search.value.toLowerCase())
  const byStatus = statusFilter.value === 'ALL' ? true : a.status === statusFilter.value
  return byName && byStatus
}))
const pendingCount = computed(() => accountsStore.pending.length)
const paidCount = computed(() => accountsStore.paid.length)
const fromPOS = computed(() => new URLSearchParams(window.location.search).get('from') === 'pos')
const accountTotal = computed(() => (selectedAccount.value?.items || []).reduce((acc, i) => acc + (i.price * i.quantity), 0))

const addByScan = () => {
  if (!selectedAccount.value || !barcodeQuery.value) return
  const match = products.value.find(p => p.barcode === barcodeQuery.value)
  if (match) {
    accountsStore.addItem(selectedAccountId.value, match)
    barcodeQuery.value = ''
  }
}

const removeItem = (productId) => {
  accountsStore.removeItem(selectedAccountId.value, productId)
}

const handlePayAccount = async () => {
  if (!selectedAccount.value || selectedAccount.value.items.length === 0) return
  if (!activeSession.value) {
    alert('No hay una caja abierta. Abra caja antes de cobrar.')
    return
  }
  loading.value = true
  try {
    await SaleService.create({
      items: selectedAccount.value.items.map(i => ({ productId: i.id, quantity: i.quantity })),
      paymentMethod: paymentMethod.value,
      referenceCode: referenceCode.value,
      userId: authStore.user.id
    })
    accountsStore.markPaid(selectedAccount.value.id)
    showPaymentDialog.value = false
    referenceCode.value = ''
    if (fromPOS.value) router.push('/pos')
  } catch (err) {
    alert(err.response?.data?.message || 'Error al cobrar la cuenta')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen p-6 bg-gray-50 dark:bg-gray-950">
    <div class="max-w-7xl mx-auto space-y-6">
      <header class="flex justify-between items-center">
        <h1 class="text-3xl font-black dark:text-white">Módulo de Cuentas</h1>
        <div class="flex gap-2">
          <ThemeToggle />
          <router-link to="/"><Button icon="pi pi-home" rounded /></router-link>
          <Button v-if="fromPOS" label="Volver al POS" icon="pi pi-arrow-left" @click="router.push('/pos')" />
        </div>
      </header>

      <div class="grid md:grid-cols-4 gap-4">
        <button class="bg-white dark:bg-gray-900 rounded-2xl p-4 text-left" @click="statusFilter = 'PENDING'">Pendientes: <b>{{ pendingCount }}</b></button>
        <button class="bg-white dark:bg-gray-900 rounded-2xl p-4 text-left" @click="statusFilter = 'PAID'">Pagadas: <b>{{ paidCount }}</b></button>
        <button class="bg-white dark:bg-gray-900 rounded-2xl p-4 text-left" @click="statusFilter = 'ALL'">Todas: <b>{{ accountsStore.accounts.length }}</b></button>
              </div>

      <div class="bg-white dark:bg-gray-900 p-4 rounded-2xl grid md:grid-cols-4 gap-3">
        <InputText v-model="clientName" placeholder="Nombre del cliente" />
        <Button label="Crear cuenta" @click="create" />
        <InputText v-model="search" placeholder="Buscar por cliente" class="md:col-span-2" />
      </div>

      <div class="grid md:grid-cols-2 gap-6">
        <div class="bg-white dark:bg-gray-900 rounded-2xl p-4">
          <h2 class="font-black mb-3 dark:text-white">Cuentas</h2>
          <div v-for="a in filteredAccounts" :key="a.id" class="p-3 mb-2 rounded-xl border dark:border-gray-800 cursor-pointer" @click="selectedAccountId = a.id">
            <div class="font-bold">{{ a.clientName }}</div>
            <div class="text-xs text-gray-500">Estado: {{ a.status === 'PENDING' ? 'Pendiente' : 'Pagada' }}</div>
          </div>
        </div>

        <div class="bg-white dark:bg-gray-900 rounded-2xl p-4" v-if="selectedAccountId">
          <h2 class="font-black mb-3 dark:text-white">Detalle de cuenta</h2>
          <InputText v-model="barcodeQuery" @keyup.enter="addByScan" placeholder="Escanear código para agregar..." class="mb-3" />
          <div class="grid grid-cols-2 gap-2 mb-4 max-h-52 overflow-auto">
            <Button v-for="p in products" :key="p.id" :label="p.name" severity="secondary" @click="accountsStore.addItem(selectedAccountId, p)" />
          </div>

          <div v-for="i in selectedAccount?.items || []" :key="i.id" class="flex justify-between items-center text-sm mb-1 border-b py-2 dark:border-gray-800">
            <div>
              <div class="font-bold">{{ i.name }}</div>
              <div class="text-xs text-blue-600">${{ (i.price * i.quantity).toFixed(2) }}</div>
            </div>
            <div class="flex items-center gap-2 bg-white dark:bg-gray-900 p-1 rounded-xl shadow-sm ring-1 ring-gray-100 dark:ring-gray-800">
              <Button icon="pi pi-minus" class="w-6 h-6 p-0" severity="secondary" text rounded @click="removeItem(i.id)" />
              <span class="font-bold text-xs min-w-[1rem] text-center dark:text-gray-300">{{ i.quantity }}</span>
              <Button icon="pi pi-plus" class="w-6 h-6 p-0" severity="secondary" text rounded @click="accountsStore.addItem(selectedAccountId, i)" />
            </div>
          </div>

          <div class="mt-4 p-3 rounded-xl bg-gray-100 dark:bg-gray-950 font-black">Total cuenta: ${{ accountTotal.toFixed(2) }}</div>

          <div class="grid grid-cols-2 gap-2 mt-3">
            <Button label="Guardar" severity="secondary" />
            <Button label="Pagar" @click="showPaymentDialog = true" :disabled="selectedAccount?.status !== 'PENDING' || (selectedAccount?.items || []).length === 0" />
          </div>
        </div>
      </div>
    </div>

    <Dialog v-model:visible="showPaymentDialog" modal header="Cobrar cuenta" :style="{ width: '400px' }" class="p-fluid dark:bg-gray-900">
      <div class="space-y-6 pt-2">
        <div>
          <label class="font-bold block mb-2">Método de pago</label>
          <div class="grid grid-cols-2 gap-2">
            <Button label="Efectivo" :severity="paymentMethod === 'CASH' ? 'success' : 'secondary'" @click="paymentMethod = 'CASH'" />
            <Button label="Transferencia" :severity="paymentMethod === 'DEUNA_TRANSFER' ? 'success' : 'secondary'" @click="paymentMethod = 'DEUNA_TRANSFER'" />
          </div>
        </div>

        <div v-if="paymentMethod === 'DEUNA_TRANSFER'">
          <label class="font-bold block mb-2">Referencia</label>
          <InputText v-model="referenceCode" placeholder="Ej. 998877" />
        </div>

        <div class="bg-gray-900 text-white p-4 rounded-xl flex justify-between">
          <span>Total</span>
          <span class="font-black text-xl">${{ accountTotal.toFixed(2) }}</span>
        </div>

        <div class="flex gap-3">
          <Button label="Cancelar" text class="flex-1" @click="showPaymentDialog = false" />
          <Button label="Confirmar pago" class="flex-1" :loading="loading" :disabled="paymentMethod === 'DEUNA_TRANSFER' && !referenceCode" @click="handlePayAccount" />
        </div>
      </div>
    </Dialog>
  </div>
</template>
