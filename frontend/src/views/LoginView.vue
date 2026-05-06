<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Button from 'primevue/button'
import Message from 'primevue/message'

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
  <div class="flex items-center justify-center min-h-screen bg-gray-100 p-4">
    <div class="w-full max-w-md bg-white rounded-xl shadow-lg p-8">
      <div class="text-center mb-8">
        <h1 class="text-3xl font-bold text-blue-600">Local Control</h1>
        <p class="text-gray-500">Acceso al Sistema POS</p>
      </div>

      <form @submit.prevent="handleLogin" class="space-y-6">
        <div class="flex flex-col gap-2">
          <label for="username" class="font-semibold">Usuario</label>
          <InputText id="username" v-model="username" placeholder="Ingresa tu usuario" required />
        </div>

        <div class="flex flex-col gap-2">
          <label for="password" class="font-semibold">Contraseña</label>
          <Password id="password" v-model="password" :feedback="false" toggleMask placeholder="••••••••" required fluid />
        </div>

        <Message v-if="authStore.error" severity="error" variant="simple">{{ authStore.error }}</Message>

        <Button type="submit" label="Iniciar Sesión" icon="pi pi-sign-in" :loading="authStore.loading" class="w-full" />
      </form>
    </div>
  </div>
</template>
