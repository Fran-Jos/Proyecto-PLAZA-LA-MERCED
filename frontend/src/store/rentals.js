import { defineStore } from 'pinia'

const STORAGE_KEY = 'localcontrol_rentals'

const defaultItems = [
  { id: 'billar', name: 'Billar' },
  { id: 'futbolin', name: 'Futbolín' },
  { id: 'voley', name: 'Cancha de vóley' },
  { id: 'futbol', name: 'Cancha de fútbol' },
  { id: 'establecimiento', name: 'Establecimiento' }
]

const load = () => {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
  } catch {
    return []
  }
}

const save = (rentals) => localStorage.setItem(STORAGE_KEY, JSON.stringify(rentals))

export const useRentalsStore = defineStore('rentals', {
  state: () => ({
    items: defaultItems,
    rentals: load()
  }),
  getters: {
    activeRentals: state => state.rentals.filter(r => r.status === 'ACTIVE'),
    finishedRentals: state => state.rentals.filter(r => r.status === 'FINISHED')
  },
  actions: {
    createRental(itemId, minutes) {
      const rentalType = this.items.find(i => i.id === itemId)
      const now = Date.now()
      const endAt = now + Number(minutes) * 60000
      const rental = {
        id: now,
        itemId,
        name: rentalType?.name || itemId,
        minutes: Number(minutes),
        startAt: now,
        endAt,
        warnedFiveMin: false,
        status: 'ACTIVE'
      }
      this.rentals.unshift(rental)
      save(this.rentals)
    },
    markWarned(id) {
      const idx = this.rentals.findIndex(r => r.id === id)
      if (idx !== -1) {
        this.rentals[idx].warnedFiveMin = true
        save(this.rentals)
      }
    },
    finishRental(id) {
      const idx = this.rentals.findIndex(r => r.id === id)
      if (idx !== -1) {
        this.rentals[idx].status = 'FINISHED'
        save(this.rentals)
      }
    }
  }
})
