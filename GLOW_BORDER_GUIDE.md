# Task Glow Border Implementation Guide

## Overview
The app now features a **glowing border system** that dynamically colors task borders based on their priority category.

## Color Scheme Applied

### Primary Colors
- **Background**: `#161616` (True Dark)
- **Primary Text**: `#F2F2F2` (White-Gray)
- **Secondary Text**: `#B7B7B7` (Medium Gray)
- **Accent/Buttons**: `#00F5A0` (Neon Green)
- **Highlight/Urgent**: `#FF006E` (Neon Pink-Red)

### Quadrant Category Colors (with glowing borders)
1. **Do First** (Urgent & Important): `#FF006E` - Neon Pink-Red
2. **Schedule** (Urgent & Not Important): `#FFA500` - Orange
3. **Delegate** (Not Urgent & Important): `#00F5A0` - Neon Green
4. **Eliminate** (Neither): `#B7B7B7` - Medium Gray

## Task Border Drawable Resources

Four border drawables have been created in `/drawable/`:

### 1. `task_border_do_first.xml`
- **Color**: `#FF006E` (Neon Pink-Red)
- **Width**: 3dp
- **Use Case**: Tasks in the "Do First" quadrant (Urgent & Important)

### 2. `task_border_schedule.xml`
- **Color**: `#FFA500` (Orange)
- **Width**: 3dp
- **Use Case**: Tasks in the "Schedule" quadrant (Urgent & Not Important)

### 3. `task_border_delegate.xml`
- **Color**: `#00F5A0` (Neon Green)
- **Width**: 3dp
- **Use Case**: Tasks in the "Delegate" quadrant (Not Urgent & Important)

### 4. `task_border_eliminate.xml`
- **Color**: `#B7B7B7` (Medium Gray)
- **Width**: 3dp
- **Use Case**: Tasks in the "Eliminate" quadrant (Neither)

## List Item Task Layout Structure

The `list_item_task.xml` has been updated with:

- **FrameLayout Container**: `task_frame_container`
  - Allows flexible background assignment for dynamic border styling
  - 8dp margin for spacing

- **LinearLayout**: `task_item_container`
  - Contains task elements (name, checkbox, edit/delete buttons)
  - Background color: `@color/background`
  - 2dp margin for border visibility

## Implementation in Java/Kotlin

To apply glowing borders dynamically based on task category:

```java
// Example implementation in your adapter or activity
public void setTaskBorder(View taskContainer, String category) {
    switch (category) {
        case "do_first":
            taskContainer.setBackground(context.getDrawable(R.drawable.task_border_do_first));
            break;
        case "schedule":
            taskContainer.setBackground(context.getDrawable(R.drawable.task_border_schedule));
            break;
        case "delegate":
            taskContainer.setBackground(context.getDrawable(R.drawable.task_border_delegate));
            break;
        case "eliminate":
            taskContainer.setBackground(context.getDrawable(R.drawable.task_border_eliminate));
            break;
    }
}
```

## Updated Files

### Color Resources
- ✅ `colors.xml` - Complete new color scheme

### Layout Files
- ✅ `activity_main.xml` - Updated with all color references
- ✅ `activity_quadrent.xml` - Fixed drawable references & colors
- ✅ `activity_login.xml` - Updated color scheme
- ✅ `activity_signup.xml` - Updated color scheme
- ✅ `activity_add_task.xml` - Updated color scheme
- ✅ `list_item_task.xml` - Enhanced with FrameLayout for border support

### Drawable Resources
- ✅ `task_border_do_first.xml` - Neon Pink-Red border
- ✅ `task_border_schedule.xml` - Orange border
- ✅ `task_border_delegate.xml` - Neon Green border
- ✅ `task_border_eliminate.xml` - Gray border

## Visual Effects

The glowing borders create a **visual hierarchy** that immediately communicates task urgency:
- **Bright Pink-Red Glow**: Critical, do immediately
- **Orange Glow**: Time-sensitive but less critical
- **Bright Green Glow**: Important but not urgent
- **Soft Gray Glow**: Low priority

## Next Steps

1. Update your `TaskAdapter` or list binding to call `setTaskBorder()` when displaying tasks
2. Ensure task objects have a `category` field indicating their quadrant
3. Test the glowing effect on different screen sizes
4. Optionally add shadow or elevation effects for enhanced glow appearance

---

All colors now use the modern neon aesthetic with the dark theme (#161616) background for maximum contrast and visual appeal.
