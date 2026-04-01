# MenuDropDownApp Integration Guide

File component:
- `app/src/main/java/com/example/se405/android_native_frontend/features/tasks_management/presentation/components/MenuSelectionPopUp.kt`

## 1. Muc tieu
`MenuDropDownApp` da duoc doi sang `Dialog`, vi vay no hien thi o giua man hinh (center modal), hanh vi chon item van giong dropdown menu nhieu lua chon.

## 2. Cach dung co ban (khong qua PopupController)
Dung khi ban da co state `expanded` ngay trong composable hien tai.

```kotlin
var expanded by remember { mutableStateOf(false) }
var items by remember {
    mutableStateOf(tags.map { it.toMenuDropDownItem() })
}

Button(onClick = { expanded = true }) {
    Text("Select tags")
}

MenuDropDownApp(
    expanded = expanded,
    items = items,
    onDismiss = { expanded = false },
    onItemClick = { clickedItem ->
        items = items.map {
            if (it.id == clickedItem.id) it.copy(selected = !it.selected) else it
        }
    }
) { tag ->
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(tag.label.icon),
            contentDescription = tag.name,
            tint = Color(tag.color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(tag.label.name)
    }
}
```

## 3. Tich hop theo co che PopupController (giong TaskDetailPopUp)
Dung khi ban muon quan ly popup theo stack toan cuc.

### Yeu cau root app
- Root da provide `LocalPopupController`
- Root da render `PopupHost(controller = popupController)`

### Mo menu tu man hinh
```kotlin
val popup = LocalPopupController.current
var items by remember {
    mutableStateOf(tags.map { it.toMenuDropDownItem() })
}

Button(onClick = {
    popup.push { onDismiss ->
        MenuDropDownApp(
            expanded = true,
            items = items,
            onDismiss = onDismiss,
            onItemClick = { clickedItem ->
                items = items.map {
                    if (it.id == clickedItem.id) it.copy(selected = !it.selected) else it
                }
            }
        ) { tag ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(tag.label.icon),
                    contentDescription = tag.name,
                    tint = Color(tag.color)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(tag.label.name)
            }
        }
    }
}) {
    Text("Select tags")
}
```

## 4. Goi y tich hop trong TaskDetailActionBasePopUp
Tai `TaskDetailActionBasePopUp`, trong nut "Select" o row `Tags`, co the thay `onClick = {}` bang logic mo popup nhu muc 3 de chon tag va cap nhat `currentTask.tags`.

Y tuong cap nhat nhanh:
- Tao danh sach `menuItems` tu danh sach tag toan bo he thong.
- Danh dau `selected = true` cho cac tag da co trong `currentTask.tags`.
- Moi lan click item, toggle selected.
- Khi dong popup, map nguoc lai thanh `List<Tag>` va gan vao `currentTask`.
