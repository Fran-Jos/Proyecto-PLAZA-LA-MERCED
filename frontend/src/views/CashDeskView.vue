<script setup>
import { ref, onMounted, computed } from 'vue'
import { CashService } from '../service/api'
import { useAuthStore } from '../store/auth'
import Card from 'primevue/card'
import InputNumber from 'primevue/inputnumber'
import Button from 'primevue/button'

const authStore = useAuthStore()
const activeSession = ref(null)
const openingBalance = ref(0)
const closingBalance = ref(0)
const loading = ref(false)
const error = ref('')

const canOpen = computed(() => !activeSession.value)

const loadActive = async () => {
  try {
    const response = await CashService.getActive(authStore.user.id)
    activeSession.value = response.data || null
  } catch {
    activeSession.value = null
  }
}

const openCash = async () => {
  loading.value = true
  error.value = ''
  try {
    await CashService.open({ userId: authStore.user.id, balance: openingBalance.value || 0 })
    await loadActive()
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
    activeSession.value = null
    closingBalance.value = 0
  } catch (err) {
    error.value = err.response?.data?.message || 'No se pudo cerrar caja'
  } finally {
    loading.value = false
  }
}

onMounted(loadActive)
</script>

<template>
  <div class="min-h-screen bg-gray-100 p-6">
    <h1 class="text-3xl font-bold text-blue-700 mb-6">Módulo de Caja</h1>

    <Card v-if="canOpen" class="max-w-xl">
      <template #title>Abrir caja</template>
      <template #content>
        <div class="space-y-4">
          <InputNumber v-model="openingBalance" mode="currency" currency="USD" locale="en-US" fluid />
          <Button label="Abrir Caja" icon="pi pi-lock-open" :loading="loading" @click="openCash" />
        </div>
      </template>
    </Card>

    <Card v-else class="max-w-xl">
      <template #title>Caja abierta</template>
      <template #content>
        <p class="mb-2">Apertura: <strong>${{ Number(activeSession.openingBalance).toFixed(2) }}</strong></p>
        <p class="mb-4">La caja está acumulando ventas en efectivo automáticamente.</p>
        <InputNumber v-model="closingBalance" mode="currency" currency="USD" locale="en-US" fluid class="mb-4" />
        <Button label="Cerrar Caja" severity="danger" icon="pi pi-lock" :loading="loading" @click="closeCash" />
      </template>
    </Card>

    <p v-if="error" class="text-red-600 mt-4">{{ error }}</p>
  </div>
</template>
