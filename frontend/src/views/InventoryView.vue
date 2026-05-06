<script setup>
import { ref, onMounted } from 'vue'
import { ProductService } from '../service/api'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'
import Select from 'primevue/select'
import Tag from 'primevue/tag'
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

onMounted(async () => {
  loadData()
})

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
</script>

<template>
  <div class="p-6 bg-gray-50 min-h-screen">
    <div class="max-w-7xl mx-auto bg-white shadow-md rounded-lg p-6">
      <div class="flex justify-between items-center mb-6">
        <h1 class="text-3xl font-bold text-gray-800">Inventario de Productos</h1>
        <div class="flex gap-2">
          <Button label="Nueva Categoría" icon="pi pi-folder" severity="secondary" @click="categoryDialog = true" />
          <Button label="Nuevo Producto" icon="pi pi-plus" @click="openNew" />
          <router-link to="/">
            <Button icon="pi pi-home" severity="info" text />
          </router-link>
        </div>
      </div>

      <DataTable :value="products" :loading="loading" paginator :rows="10" 
                 class="p-datatable-sm border rounded-lg overflow-hidden">
        <Column field="barcode" header="Código" sortable></Column>
        <Column field="name" header="Nombre" sortable></Column>
        <Column field="category.name" header="Categoría" sortable></Column>
        <Column header="Precio" sortable>
          <template #body="slotProps">
            <span class="font-bold">${{ slotProps.data.price.toFixed(2) }}</span>
          </template>
        </Column>
        <Column header="Stock" sortable>
          <template #body="slotProps">
            <Tag :value="slotProps.data.stock" :severity="getStockSeverity(slotProps.data.stock, slotProps.data.minStock)" />
          </template>
        </Column>
        <Column header="Acciones">
          <template #body="slotProps">
            <div class="flex gap-2">
              <Button icon="pi pi-pencil" severity="warn" text @click="editProduct(slotProps.data)" />
              <Button icon="pi pi-trash" severity="danger" text @click="deleteProduct(slotProps.data.id)" />
            </div>
          </template>
        </Column>
      </DataTable>
    </div>

    <!-- Dialogo Producto -->
    <Dialog v-model:visible="productDialog" :header="product.id ? 'Editar Producto' : 'Nuevo Producto'" modal class="p-fluid w-full max-w-md">
      <div class="space-y-4 pt-4">
        <div class="field">
          <label class="font-bold block mb-1">Nombre</label>
          <InputText v-model="product.name" required autofocus :class="{'p-invalid': submitted && !product.name}" />
        </div>
        <div class="field">
          <label class="font-bold block mb-1">Código de Barras</label>
          <InputText v-model="product.barcode" />
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div class="field">
            <label class="font-bold block mb-1">Precio Venta</label>
            <InputNumber v-model="product.price" mode="currency" currency="USD" locale="en-US" />
          </div>
          <div class="field">
            <label class="font-bold block mb-1">Costo</label>
            <InputNumber v-model="product.cost" mode="currency" currency="USD" locale="en-US" />
          </div>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div class="field">
            <label class="font-bold block mb-1">Stock Actual</label>
            <InputNumber v-model="product.stock" />
          </div>
          <div class="field">
            <label class="font-bold block mb-1">Stock Mínimo</label>
            <InputNumber v-model="product.minStock" />
          </div>
        </div>
        <div class="field">
          <label class="font-bold block mb-1">Categoría</label>
          <Select v-model="product.category" :options="categories" optionLabel="name" placeholder="Selecciona categoría" />
        </div>
      </div>
      <template #footer>
        <Button label="Cancelar" icon="pi pi-times" text @click="productDialog = false" />
        <Button label="Guardar" icon="pi pi-check" @click="saveProduct" />
      </template>
    </Dialog>

    <!-- Dialogo Categoría -->
    <Dialog v-model:visible="categoryDialog" header="Nueva Categoría" modal class="p-fluid w-full max-w-xs">
      <div class="field pt-4">
        <label class="font-bold block mb-1">Nombre de Categoría</label>
        <InputText v-model="category.name" placeholder="Ej: Bebidas, Snacks" />
      </div>
      <template #footer>
        <Button label="Guardar" icon="pi pi-check" @click="saveCategory" />
      </template>
    </Dialog>
  </div>
</template>
