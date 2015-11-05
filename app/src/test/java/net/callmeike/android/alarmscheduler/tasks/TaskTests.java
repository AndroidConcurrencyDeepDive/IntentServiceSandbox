package net.callmeike.android.alarmscheduler.tasks;

import net.callmeike.android.alarmscheduler.net.Client;
import net.callmeike.android.alarmscheduler.tasks.SampleTask;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.Assert.*;


/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class TaskTests {
    @Test
    public void addition_isCorrect() {
        Client client = Mockito.mock(Client.class);
        SampleTask task = new SampleTask(client);
        task.run();
        Mockito.verify(client).send(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(client);
    }
}