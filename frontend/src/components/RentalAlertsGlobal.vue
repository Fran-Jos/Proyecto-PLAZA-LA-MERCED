<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRentalsStore } from '../store/rentals'

const rentalsStore = useRentalsStore()
const activeAlert = ref(null)
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

const pushAlert = (text, severity = 'warn') => {
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
      pushAlert('Se ha cumplido el tiempo del alquiler, gracias.', 'error')
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
    <div v-if="activeAlert" :class="['fixed top-4 right-4 z-[99999] max-w-sm p-4 rounded-xl shadow-2xl text-white', activeAlert.severity === 'error' ? 'bg-red-600' : 'bg-amber-500']">
      <div class="font-black mb-1">Alerta de alquiler</div>
      <div class="text-sm">{{ activeAlert.text }}</div>
    </div>
  </transition>
</template>

<style scoped>
.fade-enter-active,.fade-leave-active{transition:all .25s ease}
.fade-enter-from,.fade-leave-to{opacity:0;transform:translateY(-8px)}
</style>
