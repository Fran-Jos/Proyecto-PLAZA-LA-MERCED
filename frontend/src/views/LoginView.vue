<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Button from 'primevue/button'
import Message from 'primevue/message'
import ThemeToggle from '../components/ThemeToggle.vue'

const router = useRouter()
const authStore = useAuthStore()

const username = ref('')
const password = ref('')

const handleLogin = async () => {
  const success = await authStore.login(username.value, password.value)
  if (success) {
    router.push('/')
  }
}
</script>

<template>
  <div class="flex flex-col items-center justify-center min-h-screen bg-gray-50 dark:bg-gray-950 p-4 transition-colors duration-300">
    <div class="absolute top-4 right-4">
      <ThemeToggle />
    </div>

    <div class="w-full max-w-md bg-white dark:bg-gray-900 rounded-3xl shadow-xl p-8 border border-gray-100 dark:border-gray-800">
      <div class="text-center mb-10">
        <div class="w-16 h-16 bg-blue-600 rounded-2xl flex items-center justify-center mx-auto mb-4 shadow-lg shadow-blue-200 dark:shadow-none">
          <i class="pi pi-shield text-3xl text-white" />
        </div>
        <h1 class="text-3xl font-black text-gray-900 dark:text-white tracking-tight">Bienvenido</h1>
        <p class="text-gray-500 dark:text-gray-400 mt-1">Ingresa tus credenciales para continuar</p>
      </div>

      <form @submit.prevent="handleLogin" class="space-y-6">
        <div class="flex flex-col gap-2">
          <label for="username" class="text-xs font-bold text-gray-700 dark:text-gray-400 uppercase tracking-widest">Usuario</label>
          <InputText id="username" v-model="username" placeholder="Tu usuario" required 
                     class="h-12 !rounded-xl dark:bg-gray-800 dark:text-white dark:border-gray-700" />
        </div>

        <div class="flex flex-col gap-2">
          <label for="password" class="text-xs font-bold text-gray-700 dark:text-gray-400 uppercase tracking-widest">Contraseña</label>
          <Password id="password" v-model="password" :feedback="false" toggleMask placeholder="••••••••" required fluid 
                    inputClass="h-12 !rounded-xl dark:bg-gray-800 dark:text-white dark:border-gray-700" />
        </div>

        <Message v-if="authStore.error" severity="error" variant="simple" class="text-sm rounded-lg">{{ authStore.error }}</Message>

        <Button type="submit" label="Acceder al Sistema" icon="pi pi-sign-in" :loading="authStore.loading" 
                class="w-full !h-14 !text-lg !font-bold !rounded-xl shadow-lg !bg-blue-600 hover:!bg-blue-700 !border-0" />
      </form>
    </div>
    
    <p class="mt-8 text-gray-400 text-xs font-medium uppercase tracking-widest">Local Control POS v1.0</p>
  </div>
</template>
