<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import ThemeToggle from '../components/ThemeToggle.vue'
import { ProductService } from '../service/api'
import { useAccountsStore } from '../store/accounts'
import { onMounted } from 'vue'

const router = useRouter()
const accountsStore = useAccountsStore()
const clientName = ref('')
const search = ref('')
const selectedAccountId = ref(null)
const products = ref([])

onMounted(async () => {
  const res = await ProductService.getAll()
  products.value = res.data
})

const create = () => {
  if (!clientName.value.trim()) return
  const account = accountsStore.createAccount(clientName.value.trim())
  selectedAccountId.value = account.id
  clientName.value = ''
}

const filteredAccounts = computed(() => accountsStore.accounts.filter(a => a.clientName.toLowerCase().includes(search.value.toLowerCase())))
const pendingCount = computed(() => accountsStore.pending.length)
const paidCount = computed(() => accountsStore.paid.length)

const fromPOS = computed(() => new URLSearchParams(window.location.search).get('from') === 'pos')
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
        <div class="bg-white dark:bg-gray-900 rounded-2xl p-4">Pendientes: <b>{{ pendingCount }}</b></div>
        <div class="bg-white dark:bg-gray-900 rounded-2xl p-4">Pagadas: <b>{{ paidCount }}</b></div>
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
          <div class="grid grid-cols-2 gap-2 mb-4 max-h-52 overflow-auto">
            <Button v-for="p in products" :key="p.id" :label="p.name" severity="secondary" @click="accountsStore.addItem(selectedAccountId, p)" />
          </div>
          <div v-for="i in accountsStore.accounts.find(a => a.id === selectedAccountId)?.items || []" :key="i.id" class="text-sm mb-1">
            {{ i.name }} x{{ i.quantity }}
          </div>
          <Button label="Marcar pagada" class="mt-4" @click="accountsStore.markPaid(selectedAccountId)" />
        </div>
      </div>
    </div>
  </div>
</template>
