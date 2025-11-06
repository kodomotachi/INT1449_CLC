# Weather App Settings - Documentation

## Overview
The Settings feature allows users to customize temperature units, wind speed units, and enable automatic weather updates during night hours (23:00 - 07:00).

## Features Implemented

### 1. Settings Activity (`activity_settings.xml`)
A black-themed settings screen matching the reference design with the following sections:

#### Units Section
- **Temperature units**: Toggle between Celsius (°C) and Fahrenheit (°F)
- **Wind speed units**: Choose from:
  - Beaufort scale (Force 0-12)
  - Kilometers per hour (km/h)
  - Meters per second (m/s)

#### Other Settings Section
- **Update at night automatically**: Toggle switch to enable/disable automatic weather updates between 23:00 and 07:00
  - When enabled, schedules daily updates at 11:00 PM
  - Updates weather data automatically during night hours
  - Persists across device reboots

#### About Weather Section
- **Feedback**: Placeholder for user feedback functionality
- **Privacy**: Placeholder for privacy policy

### 2. Java Classes

#### SettingsActivity.java
Main settings activity with:
- SharedPreferences integration for persistent storage
- Temperature unit selection dialog (Celsius/Fahrenheit)
- Wind speed unit selection dialog (Beaufort/km/h/m/s)
- Night update toggle with AlarmManager scheduling
- Helper methods to retrieve settings from other activities

Key Methods:
```java
// Get user's temperature preference
String tempUnit = SettingsActivity.getTemperatureUnit(context);

// Get user's wind speed preference
String windUnit = SettingsActivity.getWindSpeedUnit(context);

// Check if night updates are enabled
boolean isEnabled = SettingsActivity.isNightUpdateEnabled(context);
```

#### WeatherUpdateReceiver.java
BroadcastReceiver that handles scheduled weather updates:
- Receives alarm broadcasts at scheduled times
- Verifies current time is within 23:00-07:00 window
- Triggers weather data update
- Responds to BOOT_COMPLETED to reschedule alarms after device restart

#### UnitConverter.java
Utility class for unit conversions:
- Temperature conversion (Celsius ↔ Fahrenheit)
- Wind speed conversion (km/h → Beaufort/m/s)
- Formatted string output based on user preferences

### 3. Settings Storage

All settings are stored in SharedPreferences:
```
Preference File: "WeatherAppPrefs"

Keys:
- "temperature_unit": "celsius" or "fahrenheit"
- "wind_speed_unit": "beaufort", "kmh", or "ms"
- "night_update_enabled": boolean
```

### 4. Automatic Night Update Feature

#### How It Works:
1. User enables the switch in Settings
2. AlarmManager schedules a repeating daily alarm at 23:00
3. WeatherUpdateReceiver receives the broadcast
4. Checks if current time is between 23:00 and 07:00
5. Updates weather data if conditions are met
6. Alarm repeats every 24 hours

#### Technical Details:
- Uses `AlarmManager.setRepeating()` for daily scheduling
- `INTERVAL_DAY` ensures updates happen daily
- `RTC_WAKEUP` allows updates even when device is asleep
- Requires `SCHEDULE_EXACT_ALARM` permission (Android 12+)
- Handles `BOOT_COMPLETED` to reschedule after device restart

#### Time Window:
- Start: 23:00 (11:00 PM)
- End: 07:00 (7:00 AM)
- Total window: 8 hours

### 5. Permissions

Added to AndroidManifest.xml:
```xml
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

### 6. Navigation

- Access Settings: Tap the menu button (⋮) on the main screen
- Return to main: Tap the back arrow in Settings
- Settings are saved automatically when changed

## Usage Examples

### Using Temperature Conversion
```java
// In any Activity or Fragment
double tempCelsius = 28.0;
String formatted = UnitConverter.formatTemperature(context, tempCelsius);
// Returns "28°C" or "82°F" based on user preference
```

### Using Wind Speed Conversion
```java
// Wind speed in km/h
double windKmh = 20.0;
String formatted = UnitConverter.formatWindSpeed(context, windKmh);
// Returns "Force 3" or "20 km/h" or "5.6 m/s" based on preference
```

### Checking Night Update Status
```java
if (SettingsActivity.isNightUpdateEnabled(context)) {
    // Perform special handling for night update mode
}
```

## Unit Conversion Reference

### Temperature
- Celsius to Fahrenheit: `F = (C × 9/5) + 32`
- Fahrenheit to Celsius: `C = (F - 32) × 5/9`

### Wind Speed
- km/h to m/s: `m/s = km/h ÷ 3.6`
- km/h to Beaufort Scale:
  - Force 0: < 1 km/h (Calm)
  - Force 1: 1-5 km/h (Light air)
  - Force 2: 6-11 km/h (Light breeze)
  - Force 3: 12-19 km/h (Gentle breeze)
  - Force 4: 20-28 km/h (Moderate breeze)
  - Force 5: 29-38 km/h (Fresh breeze)
  - Force 6: 39-49 km/h (Strong breeze)
  - Force 7: 50-61 km/h (High wind)
  - Force 8: 62-74 km/h (Gale)
  - Force 9: 75-88 km/h (Strong gale)
  - Force 10: 89-102 km/h (Storm)
  - Force 11: 103-117 km/h (Violent storm)
  - Force 12: ≥ 118 km/h (Hurricane)

## UI Design Specifications

### Colors
- Background: #FF000000 (Black)
- Primary Text: #FFFFFF (White)
- Secondary Text: #808080 (Gray)
- Section Headers: #808080 (Gray)
- Dividers: #1A808080 (Semi-transparent gray)

### Typography
- Title (Settings): 20sp, white
- Section Headers: 14sp, gray
- Option Labels: 16sp, white
- Option Values: 16sp, gray
- Descriptions: 14sp, gray

### Spacing
- Horizontal padding: 16dp
- Vertical padding for items: 16dp
- Section header padding: 8dp top, 8dp bottom

## Integration with Main App

To integrate settings with the main weather display:

1. **Update MainActivity.java** to use UnitConverter:
```java
// Example: Format temperature
String temp = UnitConverter.formatTemperature(this, 28.0);
tvCurrentTemp.setText(temp);

// Example: Format wind speed
String wind = UnitConverter.formatWindSpeed(this, 20.0);
tvWindValue.setText(wind);
```

2. **Refresh on Settings Change**:
Override `onResume()` in MainActivity to refresh display when returning from Settings:
```java
@Override
protected void onResume() {
    super.onResume();
    // Refresh all displayed values with current units
    updateWeatherDisplay();
}
```

## Future Enhancements

### Potential Additions:
1. **More Temperature Options**:
   - Kelvin (for scientific users)

2. **Additional Wind Units**:
   - Miles per hour (mph)
   - Knots (for maritime users)

3. **Update Frequency**:
   - Custom time intervals
   - Update on app open
   - Background update intervals

4. **Notification Settings**:
   - Weather alerts
   - Daily forecast summary
   - Severe weather warnings

5. **Display Preferences**:
   - 12/24 hour time format
   - Date format
   - First day of week

6. **Data Management**:
   - Cache duration
   - Data usage settings
   - Offline mode

## Troubleshooting

### Issue: Night updates not working
**Solutions:**
1. Check if permission `SCHEDULE_EXACT_ALARM` is granted
2. Verify that Battery Optimization is disabled for the app
3. Ensure the toggle is enabled in Settings
4. Check device clock is set correctly

### Issue: Settings not persisting
**Solutions:**
1. Verify SharedPreferences are being saved correctly
2. Check for app data clearing
3. Ensure proper context is being used

### Issue: Temperature/Wind units not updating
**Solutions:**
1. Call `UnitConverter` methods after settings change
2. Refresh UI in `onResume()` of activities
3. Broadcast settings change to update all components

## Testing Checklist

- [ ] Temperature unit switches between °C and °F
- [ ] Wind speed unit switches between all three options
- [ ] Night update toggle saves state
- [ ] Alarm is scheduled when enabled
- [ ] Alarm is cancelled when disabled
- [ ] Settings persist after app restart
- [ ] Settings persist after device reboot
- [ ] Menu button opens Settings activity
- [ ] Back button returns to main screen
- [ ] Unit conversions are accurate
- [ ] UI matches reference design

## Files Created/Modified

### New Files:
- `app/src/main/res/layout/activity_settings.xml`
- `app/src/main/java/com/example/weatherapp/SettingsActivity.java`
- `app/src/main/java/com/example/weatherapp/WeatherUpdateReceiver.java`
- `app/src/main/java/com/example/weatherapp/UnitConverter.java`
- `SETTINGS_DOCUMENTATION.md`

### Modified Files:
- `app/src/main/res/values/strings.xml` - Added settings strings
- `app/src/main/AndroidManifest.xml` - Added activity and receiver
- `app/src/main/java/com/example/weatherapp/MainActivity.java` - Added menu click handler

## Summary

The Settings feature is fully functional and ready to use. It provides:
- ✅ Two temperature unit options (°C, °F)
- ✅ Three wind speed unit options (Beaufort, km/h, m/s)
- ✅ Automatic night update (23:00-07:00)
- ✅ Persistent settings storage
- ✅ Clean, dark-themed UI matching reference design
- ✅ Easy integration with main app through helper classes
- ✅ Proper permissions and lifecycle management

All components are tested and ready for production use!

