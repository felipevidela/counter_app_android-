package com.example.counter_app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Componente de navegación breadcrumb para indicar la ubicación actual en la jerarquía.
 *
 * Muestra una ruta de navegación tipo "Inicio > Categoría > Item Actual" donde cada
 * elemento excepto el último es clickeable para navegar hacia atrás.
 *
 * @param items Lista de nombres de las páginas en la ruta de navegación
 * @param onItemClick Callback que se invoca al hacer click en un item (recibe el índice)
 */
@Composable
fun BreadcrumbNavigation(
    items: List<String>,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            val isLast = index == items.lastIndex

            Text(
                text = item,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isLast) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal,
                modifier = if (!isLast) {
                    Modifier.clickable { onItemClick(index) }
                } else {
                    Modifier
                }
            )

            if (!isLast) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}
