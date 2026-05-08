<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import Button from 'primevue/button'
import InputNumber from 'primevue/inputnumber'
import Message from 'primevue/message'
import ThemeToggle from '../components/ThemeToggle.vue'
import { useRentalsStore } from '../store/rentals'

const rentalsStore = useRentalsStore()
const selectedItem = ref('billar')
const durationMinutes = ref(30)
const notifications = ref([])
const now = ref(Date.now())
let timer = null

const beep = () => {
  const ctx = new (window.AudioContext || window.webkitAudioContext)()
  const osc = ctx.createOscillator()
  osc.connect(ctx.destination)
  osc.frequency.value = 880
  osc.start()
  setTimeout(() => { osc.stop(); ctx.close() }, 300)
}

const evaluateRentals = () => {
  rentalsStore.activeRentals.forEach(r => {
    const msLeft = r.endAt - now.value
    const minLeft = Math.ceil(msLeft / 60000)
    if (msLeft <= 0) {
      notifications.value.unshift({ id: `${r.id}-end`, text: 'Se ha cumplido el tiempo del alquiler, gracias.', severity: 'error' })
      beep()
      rentalsStore.finishRental(r.id)
      return
    }
    if (!r.warnedFiveMin && minLeft <= 5) {
      notifications.value.unshift({ id: `${r.id}-warn`, text: `Al alquiler de ${r.name.toLowerCase()} le quedan 5 minutos.`, severity: 'warn' })
      rentalsStore.markWarned(r.id)
    }
  })
}

const startRental = () => {
  if (!durationMinutes.value || durationMinutes.value <= 0) return
  rentalsStore.createRental(selectedItem.value, durationMinutes.value)
}

const activeRows = computed(() => rentalsStore.activeRentals.map(r => ({ ...r, msLeft: Math.max(0, r.endAt - now.value) })))

onMounted(() => {
  timer = setInterval(() => { now.value = Date.now(); evaluateRentals() }, 1000)
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<template>
  <div class="min-h-screen p-6 md:p-10 bg-gray-50 dark:bg-gray-950">
    <div class="max-w-6xl mx-auto space-y-6">
      <header class="flex justify-between items-center">
        <h1 class="text-3xl font-black dark:text-white">Módulo de Alquiler</h1>
        <div class="flex items-center gap-3">
          <ThemeToggle />
          <router-link to="/"><Button icon="pi pi-home" label="Volver" rounded /></router-link>
        </div>
      </header>

      <div class="bg-white dark:bg-gray-900 p-6 rounded-2xl space-y-4 border dark:border-gray-800">
        <label class="font-bold dark:text-gray-200">Tipo de alquiler</label>
        <select v-model="selectedItem" class="w-full h-11 rounded-xl px-3 bg-gray-50 dark:bg-gray-950 dark:text-white border dark:border-gray-800">
          <option v-for="i in rentalsStore.items" :key="i.id" :value="i.id">{{ i.name }}</option>
        </select>
        <label class="font-bold dark:text-gray-200">Duración (minutos)</label>
        <InputNumber v-model="durationMinutes" :min="1" showButtons class="w-full" />
        <Button label="Iniciar alquiler" icon="pi pi-clock" class="w-full" @click="startRental" />
      </div>

      <div class="grid md:grid-cols-2 gap-6">
        <div class="bg-white dark:bg-gray-900 p-6 rounded-2xl border dark:border-gray-800">
          <h2 class="font-black mb-4 dark:text-white">Alquileres activos</h2>
          <div v-if="activeRows.length === 0" class="text-gray-400">Sin alquileres activos.</div>
          <div v-for="r in activeRows" :key="r.id" class="p-3 rounded-xl bg-gray-50 dark:bg-gray-950 mb-2">
            <div class="font-bold dark:text-gray-100">{{ r.name }}</div>
            <div class="text-sm text-gray-500">Tiempo restante: {{ Math.ceil(r.msLeft / 60000) }} min</div>
          </div>
        </div>

        <div class="bg-white dark:bg-gray-900 p-6 rounded-2xl border dark:border-gray-800">
          <h2 class="font-black mb-4 dark:text-white">Alertas</h2>
          <Message v-for="n in notifications.slice(0,8)" :key="n.id" :severity="n.severity" class="mb-2">{{ n.text }}</Message>
        </div>
      </div>
    </div>
  </div>
</template>
