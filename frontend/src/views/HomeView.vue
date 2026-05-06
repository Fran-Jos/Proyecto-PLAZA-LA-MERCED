<script setup>
import { useAuthStore } from '../store/auth';
import { useRouter } from 'vue-router';
import Button from 'primevue/button';

const authStore = useAuthStore();
const router = useRouter();

const handleLogout = () => {
  authStore.logout();
  router.push('/login');
};
</script>

<template>
  <div class="flex flex-col items-center justify-center min-h-screen p-4 text-center">
    <div class="absolute top-4 right-4 flex items-center gap-4">
      <span class="text-gray-600 font-medium">Hola, {{ authStore.user?.username }} ({{ authStore.user?.role }})</span>
      <Button label="Cerrar Sesión" icon="pi pi-sign-out" severity="danger" text @click="handleLogout" />
    </div>

    <h1 class="text-4xl font-bold text-blue-600 mb-4">Local Control POS</h1>
    <p class="text-xl text-gray-600 mb-8">Gestión inteligente para tu local comercial.</p>
    
    <div class="flex gap-4">
      <router-link to="/pos">
        <Button label="Ir al POS" icon="pi pi-shopping-cart" />
      </router-link>
      <router-link to="/inventory" v-if="authStore.isAdmin">
        <Button label="Inventario" icon="pi pi-box" severity="secondary" />
      </router-link>
      <router-link to="/reports" v-if="authStore.isAdmin">
        <Button label="Reportes" icon="pi pi-chart-bar" severity="info" />
      </router-link>
    </div>
  </div>
</template>
