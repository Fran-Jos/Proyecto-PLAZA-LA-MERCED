<script setup>
import { ref, onMounted, watch } from 'vue'
import { ProductService } from '../service/api'
import { FilterMatchMode } from '@primevue/core/api'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'
import Select from 'primevue/select'
import Tag from 'primevue/tag'
import ThemeToggle from '../components/ThemeToggle.vue'
import { useToast } from 'primevue/usetoast'
import Toast from 'primevue/toast'

const products = ref([])
const categories = ref([])
const productDialog = ref(false)
const categoryDialog = ref(false)
const product = ref({})
const category = ref({})
const submitted = ref(false)
const loading = ref(false)
const inventorySearchInput = ref(null)

const filters = ref({
  global: { value: null, matchMode: FilterMatchMode.CONTAINS },
})

onMounted(async () => {
  loadData()
  focusSearch()
})

// Refocar buscador al cerrar el diálogo
watch(productDialog, (val) => {
  if (!val) focusSearch()
})

const focusSearch = () => {
  setTimeout(() => {
    if (inventorySearchInput.value) {
      inventorySearchInput.value.$el.querySelector('input')?.focus()
    }
  }, 300)
}

const handleGlobalSearch = () => {
  const query = filters.value.global.value
  if (!query) return
  
  const exactMatch = products.value.find(p => p.barcode === query)
  if (exactMatch) {
    editProduct(exactMatch)
    filters.value.global.value = '' // Limpiar para el siguiente escaneo
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const [pRes, cRes] = await Promise.all([
      ProductService.getAll(),
      ProductService.getCategories()
    ])
    products.value = pRes.data
    categories.value = cRes.data
  } finally {
    loading.value = false
  }
}

const openNew = () => {
  product.value = { stock: 0, minStock: 5 }
  submitted.value = false
  productDialog.value = true
}

const editProduct = (prod) => {
  product.value = { ...prod }
  productDialog.value = true
}

const saveProduct = async () => {
  submitted.value = true
  if (product.value.name && product.value.price != null && product.value.category) {
    try {
      await ProductService.save(product.value)
      productDialog.value = false
      loadData()
    } catch (err) {
      alert('Error al guardar el producto')
    }
  }
}

const deleteProduct = async (id) => {
  if (confirm('¿Estás seguro de eliminar este producto?')) {
    await ProductService.delete(id)
    loadData()
  }
}

const saveCategory = async () => {
  if (category.value.name) {
    await ProductService.saveCategory(category.value)
    categoryDialog.value = false
    category.value = {}
    loadData()
  }
}

const getStockSeverity = (stock, minStock) => {
  if (stock <= 0) return 'danger'
  if (stock <= minStock) return 'warn'
  return 'success'
}

const printLabel = (prod) => {
  const printWindow = window.open('', '_blank')
  printWindow.document.write(`
    <html>
      <head>
        <title>Imprimir Etiqueta - ${prod.name}</title>
        <style>
          body { font-family: sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }
          .label { border: 1px solid #000; padding: 20px; text-align: center; width: 300px; }
          .name { font-size: 1.2rem; font-weight: bold; margin-bottom: 5px; }
          .price { font-size: 2rem; font-weight: 900; margin-bottom: 10px; }
          .barcode { font-size: 0.8rem; font-family: 'Libre Barcode 39', cursive; font-size: 3rem; }
          .code-text { font-size: 0.7rem; margin-top: 5px; }
        </style>
        <link href="https://fonts.googleapis.com/css2?family=Libre+Barcode+39&display=swap" rel="stylesheet">
      </head>
      <body>
        <div class="label">
          <div class="name">${prod.name}</div>
          <div class="price">$${prod.price.toFixed(2)}</div>
          <div class="barcode">*${prod.barcode || '0000'}*</div>
          <div class="code-text">${prod.barcode || '-'}</div>
        </div>
        <script>
          setTimeout(() => {
            window.print();
            window.close();
          }, 500);
        <\/script>
      </body>
    </html>
  `)
  printWindow.document.close()
}
</script>

<template>
  <div class="p-4 md:p-8 bg-gray-50 dark:bg-gray-950 min-h-screen font-sans text-gray-900 dark:text-gray-100 transition-colors duration-300">
    <div class="max-w-7xl mx-auto space-y-6">
      
      <header class="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 class="text-3xl font-extrabold tracking-tight text-gray-900 dark:text-white">Inventario</h1>
          <p class="text-gray-500 dark:text-gray-400 mt-1">Gestión de productos, precios y existencias.</p>
        </div>
        <div class="flex items-center gap-2">
          <ThemeToggle />
          <Button label="Categoría" icon="pi pi-folder" severity="secondary" text @click="categoryDialog = true" class="dark:text-gray-300" />
          <Button label="Nuevo Producto" icon="pi pi-plus" @click="openNew" rounded />
          <router-link to="/"><Button icon="pi pi-home" severity="secondary" text rounded /></router-link>
        </div>
      </header>

      <div class="mb-4 relative">
        <i class="pi pi-search absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
        <InputText ref="inventorySearchInput" v-model="filters['global'].value" placeholder="Escanear o buscar producto para editar..." 
                   @input="handleGlobalSearch"
                   class="w-full pl-12 h-12 rounded-xl border-0 shadow-sm ring-1 ring-gray-200 dark:ring-gray-800 bg-white dark:bg-gray-900 focus:ring-2 focus:ring-blue-500 transition-all dark:text-white" />
      </div>

      <div class="bg-white dark:bg-gray-900 shadow-sm rounded-2xl border border-gray-100 dark:border-gray-800 overflow-hidden">
        <DataTable :value="products" :loading="loading" paginator :rows="10" 
                   v-model:filters="filters"
                   class="p-datatable-sm" responsiveLayout="stack">
          <Column field="barcode" header="Código" sortable>
            <template #body="slotProps"><span class="text-xs font-mono dark:text-gray-400">{{ slotProps.data.barcode || '-' }}</span></template>
          </Column>
          <Column field="name" header="Nombre" sortable>
            <template #body="slotProps"><span class="font-bold dark:text-gray-200">{{ slotProps.data.name }}</span></template>
          </Column>
          <Column field="category.name" header="Categoría" sortable>
            <template #body="slotProps"><Tag :value="slotProps.data.category?.name" severity="secondary" class="text-[10px]" /></template>
          </Column>
          <Column header="Precio" sortable>
            <template #body="slotProps">
              <span class="font-black text-blue-600 dark:text-blue-400">${{ slotProps.data.price.toFixed(2) }}</span>
            </template>
          </Column>
          <Column header="Stock" sortable>
            <template #body="slotProps">
              <div class="flex items-center gap-2">
                <Tag :value="slotProps.data.stock" :severity="getStockSeverity(slotProps.data.stock, slotProps.data.minStock)" />
                <span v-if="slotProps.data.stock <= slotProps.data.minStock" class="text-[10px] text-red-500 font-bold uppercase">Bajo</span>
              </div>
            </template>
          </Column>
          <Column header="Acciones">
            <template #body="slotProps">
              <div class="flex gap-1">
                <Button icon="pi pi-print" severity="secondary" text rounded @click="printLabel(slotProps.data)" v-tooltip="'Imprimir Etiqueta'" />
                <Button icon="pi pi-pencil" severity="warn" text rounded @click="editProduct(slotProps.data)" />
                <Button icon="pi pi-trash" severity="danger" text rounded @click="deleteProduct(slotProps.data.id)" />
              </div>
            </template>
          </Column>
        </DataTable>
      </div>
    </div>

    <!-- Dialogo Producto -->
    <Dialog v-model:visible="productDialog" :header="product.id ? 'Editar Producto' : 'Nuevo Producto'" modal class="p-fluid w-full max-w-md dark:bg-gray-900">
      <div class="space-y-4 pt-4">
        <div class="field">
          <label class="font-bold block mb-1 text-sm dark:text-gray-300">Nombre del Producto</label>
          <InputText v-model="product.name" required autofocus :class="{'p-invalid': submitted && !product.name}" class="dark:bg-gray-800 dark:text-white dark:border-gray-700" />
        </div>
        <div class="field">
          <label class="font-bold block mb-1 text-sm dark:text-gray-300">Código de Barras</label>
          <InputText v-model="product.barcode" class="dark:bg-gray-800 dark:text-white dark:border-gray-700" />
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div class="field">
            <label class="font-bold block mb-1 text-sm dark:text-gray-300">Precio Venta</label>
            <InputNumber v-model="product.price" mode="currency" currency="USD" locale="en-US" inputClass="dark:bg-gray-800 dark:text-white dark:border-gray-700" />
          </div>
          <div class="field">
            <label class="font-bold block mb-1 text-sm dark:text-gray-300">Costo</label>
            <InputNumber v-model="product.cost" mode="currency" currency="USD" locale="en-US" inputClass="dark:bg-gray-800 dark:text-white dark:border-gray-700" />
          </div>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div class="field">
            <label class="font-bold block mb-1 text-sm dark:text-gray-300">Stock Actual</label>
            <InputNumber v-model="product.stock" inputClass="dark:bg-gray-800 dark:text-white dark:border-gray-700" />
          </div>
          <div class="field">
            <label class="font-bold block mb-1 text-sm dark:text-gray-300">Stock Mínimo</label>
            <InputNumber v-model="product.minStock" inputClass="dark:bg-gray-800 dark:text-white dark:border-gray-700" />
          </div>
        </div>
        <div class="field">
          <label class="font-bold block mb-1 text-sm dark:text-gray-300">Categoría</label>
          <Select v-model="product.category" :options="categories" optionLabel="name" placeholder="Seleccionar..." class="dark:bg-gray-800 dark:text-white dark:border-gray-700" />
        </div>
      </div>
      <template #footer>
        <Button label="Cancelar" icon="pi pi-times" text @click="productDialog = false" class="dark:text-gray-400" />
        <Button label="Guardar Producto" icon="pi pi-check" @click="saveProduct" class="font-bold" />
      </template>
    </Dialog>

    <!-- Dialogo Categoría -->
    <Dialog v-model:visible="categoryDialog" header="Nueva Categoría" modal class="p-fluid w-full max-w-xs dark:bg-gray-900">
      <div class="field pt-4">
        <label class="font-bold block mb-1 text-sm dark:text-gray-300">Nombre</label>
        <InputText v-model="category.name" placeholder="Ej: Bebidas" class="dark:bg-gray-800 dark:text-white dark:border-gray-700" />
      </div>
      <template #footer>
        <Button label="Guardar" icon="pi pi-check" @click="saveCategory" class="w-full" />
      </template>
    </Dialog>
  </div>
</template>

<style scoped>
:deep(.p-datatable-header) { background: transparent; }
:deep(.p-datatable .p-datatable-thead > tr > th) {
  background: transparent;
  color: #6b7280;
  font-size: 0.75rem;
  text-transform: uppercase;
}
.dark :deep(.p-datatable .p-datatable-tbody > tr) {
  background: transparent;
  color: #d1d5db;
}
</style>
