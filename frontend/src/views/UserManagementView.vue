<script setup>
import { ref } from 'vue'
import { AdminService } from '../service/api'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Select from 'primevue/select'
import Button from 'primevue/button'

const form = ref({ username: '', password: '', role: 'VENDEDOR' })
const loading = ref(false)
const message = ref('')
const roles = [{ label: 'Vendedor', value: 'VENDEDOR' }, { label: 'Administrador', value: 'ADMIN' }]

const createUser = async () => {
  loading.value = true
  message.value = ''
  try {
    await AdminService.createUser(form.value)
    message.value = 'Usuario creado correctamente'
    form.value = { username: '', password: '', role: 'VENDEDOR' }
  } catch (err) {
    message.value = err.response?.data?.message || 'No se pudo crear el usuario'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-100 p-6">
    <div class="max-w-xl bg-white rounded-xl p-6 shadow">
      <h1 class="text-2xl font-bold text-blue-700 mb-4">Crear usuarios</h1>
      <div class="space-y-4">
        <InputText v-model="form.username" placeholder="Usuario" />
        <Password v-model="form.password" :feedback="false" toggleMask placeholder="Contraseña" fluid />
        <Select v-model="form.role" :options="roles" optionLabel="label" optionValue="value" placeholder="Rol" class="w-full" />
        <Button label="Crear usuario" icon="pi pi-user-plus" :loading="loading" @click="createUser" />
      </div>
      <p v-if="message" class="mt-3 text-sm">{{ message }}</p>
    </div>
  </div>
</template>
