package freifunk.bremen.de.mobilemeshviewer.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowAlarmManager;
import org.robolectric.shadows.ShadowApplication;

import java.util.List;

import javax.inject.Inject;

import freifunk.bremen.de.mobilemeshviewer.PreferenceController;
import freifunk.bremen.de.mobilemeshviewer.RobolectricTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

public class AlarmControllerTest extends RobolectricTest {

    @Inject
    AlarmController classUnderTest;
    @Mock
    private PreferenceController preferenceController;
    private ShadowAlarmManager shadowAlarmManager;
    private AlarmManager alarmManager;

    @Override
    public void setUp() throws Exception {
        alarmManager = Mockito.spy((AlarmManager) RuntimeEnvironment.application.getSystemService(Context.ALARM_SERVICE));
        shadowAlarmManager = shadowOf(alarmManager);
        super.setUp();
    }

    @Test
    public void testChangeNodeAlarm() throws Exception {
        // Given
        Mockito.when(preferenceController.getAlarmInterval()).thenReturn(1000L);

        // When
        classUnderTest.changeNodeAlarm();

        // Then
        assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNotNull();
        Mockito.verify(alarmManager).setInexactRepeating(Mockito.anyInt(), Mockito.anyLong(), Mockito.anyLong(), Mockito.any(PendingIntent.class));
        Mockito.verify(preferenceController).getAlarmInterval();
    }

    @Test
    public void testChangeNodeAlarmNegative() throws Exception {
        // Given
        Mockito.when(preferenceController.getAlarmInterval()).thenReturn(-1L);

        // When
        classUnderTest.changeNodeAlarm();

        // Then
        assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNull();
        Mockito.verify(alarmManager, Mockito.never()).setInexactRepeating(Mockito.anyInt(), Mockito.anyLong(), Mockito.anyLong(), Mockito.any(PendingIntent.class));
        Mockito.verify(preferenceController).getAlarmInterval();
    }

    @Test
    public void testChangeNodeAlarmShouldCancelFirstAlarm() throws Exception {
        // Given
        Mockito.when(preferenceController.getAlarmInterval()).thenReturn(1000L, 10000L);

        // When
        classUnderTest.changeNodeAlarm();
        classUnderTest.changeNodeAlarm();

        // Then
        final List<ShadowAlarmManager.ScheduledAlarm> scheduledAlarms = shadowAlarmManager.getScheduledAlarms();
        assertThat(scheduledAlarms).isNotNull().hasSize(1);
        assertThat(scheduledAlarms.get(0).interval).isEqualTo(10000L);
    }

    @Test
    public void testStopNodeAlarm() throws Exception {
        // Given
        Mockito.when(preferenceController.getAlarmInterval()).thenReturn(1000L);

        // When
        classUnderTest.changeNodeAlarm();
        classUnderTest.stopNodeAlarm();

        // Then
        assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNull();
        Mockito.verify(alarmManager).setInexactRepeating(Mockito.anyInt(), Mockito.anyLong(), Mockito.anyLong(), Mockito.any(PendingIntent.class));
        Mockito.verify(alarmManager).cancel(Mockito.any(PendingIntent.class));
        Mockito.verify(preferenceController).getAlarmInterval();
    }

    @Test
    public void testSendAlarmImmediately() throws Exception {
        // When
        classUnderTest.sendAlarmImmediately();

        // Then
        assertThat(ShadowApplication.getInstance().getBroadcastIntents()).isNotNull().hasSize(1);
    }

    @Override
    public TestModule getModuleForInjection() {
        return new CustomTestModule();
    }

    @Override
    public void inject() {
        component.inject(this);
    }

    private class CustomTestModule extends TestModule {

        @Override
        public PreferenceController providesPreferenceController(SharedPreferences sharedPreferences) {
            return preferenceController;
        }

        @Override
        public AlarmManager providesAlarmManager() {
            return alarmManager;
        }
    }
}