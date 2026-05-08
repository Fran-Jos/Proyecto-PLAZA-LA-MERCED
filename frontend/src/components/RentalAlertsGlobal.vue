<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRentalsStore } from '../store/rentals'

const rentalsStore = useRentalsStore()
const activeAlert = ref(null)
const blockingEndAlert = ref(null)
let timer = null

const beep = () => {
  try {
    const ctx = new (window.AudioContext || window.webkitAudioContext)()
    const osc = ctx.createOscillator()
    osc.connect(ctx.destination)
    osc.frequency.value = 880
    osc.start()
    setTimeout(() => { osc.stop(); ctx.close() }, 300)
  } catch (e) {
    console.warn('No se pudo reproducir alerta sonora', e)
  }
}

const pushAlert = (text, severity = 'warn', blocking = false) => {
  if (blocking) {
    blockingEndAlert.value = { text, severity }
    beep()
    return
  }
  activeAlert.value = { text, severity }
  beep()
  setTimeout(() => {
    if (activeAlert.value?.text === text) activeAlert.value = null
  }, 6000)
}

const tick = () => {
  const now = Date.now()
  rentalsStore.activeRentals.forEach(r => {
    const msLeft = r.endAt - now
    const minLeft = Math.ceil(msLeft / 60000)

    if (msLeft <= 0) {
      pushAlert('Se ha cumplido el tiempo del alquiler, gracias.', 'error', true)
      rentalsStore.finishRental(r.id)
      return
    }

    if (!r.warnedFiveMin && minLeft <= 5) {
      pushAlert(`Al alquiler de ${r.name.toLowerCase()} le quedan 5 minutos.`, 'warn')
      rentalsStore.markWarned(r.id)
    }
  })
}

onMounted(() => {
  timer = setInterval(tick, 1000)
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<template>
  <transition name="fade">
    <div v-if="activeAlert" :class="['fixed top-6 left-1/2 -translate-x-1/2 z-[99999] w-[95vw] max-w-4xl p-6 rounded-2xl shadow-2xl text-white text-center', activeAlert.severity === 'error' ? 'bg-red-600' : 'bg-amber-500']">
      <div class="font-black mb-1">Alerta de alquiler</div>
      <div class="text-sm">{{ activeAlert.text }}</div>
    </div>
  </transition>
  <div v-if="blockingEndAlert" class="fixed inset-0 z-[100000] bg-black/80 flex items-center justify-center p-6">
    <div class="w-full max-w-2xl bg-red-600 text-white rounded-3xl p-10 shadow-2xl text-center border-4 border-white">
      <div class="text-4xl md:text-5xl font-black mb-4">⏰ TIEMPO FINALIZADO</div>
      <div class="text-xl md:text-2xl font-bold mb-8">{{ blockingEndAlert.text }}</div>
      <button class="px-10 py-4 rounded-2xl bg-white text-red-700 text-2xl font-black hover:scale-105 transition" @click="blockingEndAlert = null">OK</button>
    </div>
  </div>
</template>

<style scoped>
.fade-enter-active,.fade-leave-active{transition:all .25s ease}
.fade-enter-from,.fade-leave-to{opacity:0;transform:translateY(-8px)}
</style>
