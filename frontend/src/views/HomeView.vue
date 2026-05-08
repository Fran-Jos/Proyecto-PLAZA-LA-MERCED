<script setup>
import { useAuthStore } from '../store/auth';
import { useRouter } from 'vue-router';
import Button from 'primevue/button';
import ThemeToggle from '../components/ThemeToggle.vue';

const authStore = useAuthStore();
const router = useRouter();

const handleLogout = () => {
  authStore.logout();
  router.push('/login');
};
</script>

<template>
  <div class="flex flex-col items-center justify-center min-h-screen p-4 text-center bg-gray-50 dark:bg-gray-950 text-gray-900 dark:text-gray-100 transition-colors duration-300">
    <div class="absolute top-4 right-4 flex items-center gap-4">
      <ThemeToggle />
      <span class="text-gray-600 dark:text-gray-400 font-medium hidden md:inline">Hola, {{ authStore.user?.username }}</span>
      <Button label="Salir" icon="pi pi-sign-out" severity="danger" text @click="handleLogout" />
    </div>

    <div class="mb-10">
      <div class="w-24 h-24 bg-blue-600 rounded-3xl flex items-center justify-center mx-auto mb-6 shadow-xl shadow-blue-200 dark:shadow-none">
        <i class="pi pi-box text-5xl text-white" />
      </div>
      <h1 class="text-5xl font-black text-gray-900 dark:text-white mb-4 tracking-tighter">Local Control</h1>
      <p class="text-xl text-gray-500 dark:text-gray-400 max-w-md mx-auto">Gestión inteligente para tu negocio.</p>
    </div>
    
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6 max-w-4xl w-full">
      <router-link to="/pos" class="group">
        <div class="p-8 bg-white dark:bg-gray-900 rounded-3xl shadow-sm hover:shadow-xl border border-gray-100 dark:border-gray-800 transition-all hover:-translate-y-1">
          <i class="pi pi-shopping-cart text-4xl text-blue-600 mb-4" />
          <h2 class="text-xl font-bold mb-2">Punto de Venta</h2>
          <p class="text-sm text-gray-400">Ventas rápidas y control de stock.</p>
        </div>
      </router-link>

      <router-link to="/cash" class="group">
        <div class="p-8 bg-white dark:bg-gray-900 rounded-3xl shadow-sm hover:shadow-xl border border-gray-100 dark:border-gray-800 transition-all hover:-translate-y-1">
          <i class="pi pi-wallet text-4xl text-emerald-600 mb-4" />
          <h2 class="text-xl font-bold mb-2">Caja y Arqueos</h2>
          <p class="text-sm text-gray-400">Control de flujo de efectivo.</p>
        </div>
      </router-link>

      <router-link to="/inventory" v-if="authStore.isAdmin" class="group">
        <div class="p-8 bg-white dark:bg-gray-900 rounded-3xl shadow-sm hover:shadow-xl border border-gray-100 dark:border-gray-800 transition-all hover:-translate-y-1">
          <i class="pi pi-box text-4xl text-amber-600 mb-4" />
          <h2 class="text-xl font-bold mb-2">Inventario</h2>
          <p class="text-sm text-gray-400">Productos y stock mínimo.</p>
        </div>
      </router-link>

      <router-link to="/reports" v-if="authStore.isAdmin" class="group">
        <div class="p-8 bg-white dark:bg-gray-900 rounded-3xl shadow-sm hover:shadow-xl border border-gray-100 dark:border-gray-800 transition-all hover:-translate-y-1">
          <i class="pi pi-chart-bar text-4xl text-purple-600 mb-4" />
          <h2 class="text-xl font-bold mb-2">Reportes</h2>
          <p class="text-sm text-gray-400">Analítica de rendimiento.</p>
        </div>
      </router-link>

      <router-link to="/users" v-if="authStore.isAdmin" class="group">
        <div class="p-8 bg-white dark:bg-gray-900 rounded-3xl shadow-sm hover:shadow-xl border border-gray-100 dark:border-gray-800 transition-all hover:-translate-y-1">
          <i class="pi pi-users text-4xl text-indigo-600 mb-4" />
          <h2 class="text-xl font-bold mb-2">Usuarios</h2>
          <p class="text-sm text-gray-400">Administración de accesos.</p>
        </div>
      </router-link>

      <router-link to="/price-checker" class="group">
        <div class="p-8 bg-white dark:bg-gray-900 rounded-3xl shadow-sm hover:shadow-xl border border-gray-100 dark:border-gray-800 transition-all hover:-translate-y-1">
          <i class="pi pi-barcode text-4xl text-rose-600 mb-4" />
          <h2 class="text-xl font-bold mb-2">Verificador</h2>
          <p class="text-sm text-gray-400">Consulta de precios rápida.</p>
        </div>
      </router-link>

      <router-link to="/rentals" class="group">
        <div class="p-8 bg-white dark:bg-gray-900 rounded-3xl shadow-sm hover:shadow-xl border border-gray-100 dark:border-gray-800 transition-all hover:-translate-y-1">
          <i class="pi pi-clock text-4xl text-orange-600 mb-4" />
          <h2 class="text-xl font-bold mb-2">Alquiler</h2>
          <p class="text-sm text-gray-400">Control de tiempos y alertas.</p>
        </div>
      </router-link>

      <router-link to="/accounts" class="group">
        <div class="p-8 bg-white dark:bg-gray-900 rounded-3xl shadow-sm hover:shadow-xl border border-gray-100 dark:border-gray-800 transition-all hover:-translate-y-1">
          <i class="pi pi-users text-4xl text-teal-600 mb-4" />
          <h2 class="text-xl font-bold mb-2">Cuentas</h2>
          <p class="text-sm text-gray-400">Clientes pendientes y pagos.</p>
        </div>
      </router-link>

      <router-link to="/stock-entry" v-if="authStore.isAdmin" class="group">
        <div class="p-8 bg-white dark:bg-gray-900 rounded-3xl shadow-sm hover:shadow-xl border border-gray-100 dark:border-gray-800 transition-all hover:-translate-y-1">
          <i class="pi pi-download text-4xl text-cyan-600 mb-4" />
          <h2 class="text-xl font-bold mb-2">Carga Stock</h2>
          <p class="text-sm text-gray-400">Entrada rápida de mercadería.</p>
        </div>
      </router-link>
    </div>
  </div>
</template>
