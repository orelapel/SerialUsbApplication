package com.example.serialusbapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String ACTION_USB_PERMISSION =
            "com.android.example.USB_PERMISSION";
    UsbSerialPort port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void OnClickSend(View view) {
        // Get the value from the EditText
        EditText chosenNumber = (EditText) findViewById(R.id.text);
        String numToString = chosenNumber.getText().toString();
        boolean isNumeric = isNumeric(numToString);

        // If the value is valid
        if (!numToString.equals("") && isNumeric) {
            // Write value to serial and delete it from EditText
            try {
                port.write(numToString.getBytes(), 10);
            } catch (IOException e) {
                e.printStackTrace();
            }
            chosenNumber.setText("");

            byte[] bufferRead = new byte[10];
            do {
                // Get the value from serial and show the result
                try {
                    port.read(bufferRead, 100);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } while (bufferRead[0] == 0);
            String resultToString = new String(bufferRead, StandardCharsets.UTF_8);
            TextView result = (TextView) findViewById(R.id.result);
            result.setText(resultToString);
        } else if (numToString.equals("")) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "you need to enter something", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            chosenNumber.setText("");
            Toast toast = Toast.makeText(getApplicationContext(),
                    "you can only enter a number", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void OnClickConnent(View view) {
        // Find all available drivers from attached devices.
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            return;
        }

        // Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);
        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(ACTION_USB_PERMISSION), 0);
        manager.requestPermission(driver.getDevice(), permissionIntent);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            // add UsbManager.requestPermission(driver.getDevice(), ..) handling here
            return;
        }

        port = driver.getPorts().get(0); // Most devices have just one port (port 0)
        try {
            port.open(connection);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
        } catch (IOException e) {
            e.printStackTrace();
        }


        Toast toast = Toast.makeText(getApplicationContext(),
                "connected successfully to serial", Toast.LENGTH_SHORT);
        toast.show();

        // Enable the send button
        Button sendButton = (Button) findViewById(R.id.send);
        sendButton.setEnabled(true);
    }

    public void OnClickDisonnent(View view) {
        try {
            port.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Toast toast = Toast.makeText(getApplicationContext(),
                "disconnected successfully from serial", Toast.LENGTH_SHORT);
        toast.show();

        // Disable the send button
        Button sendButton = (Button) findViewById(R.id.send);
        sendButton.setEnabled(false);

        // Initialize the result
        String init = getResources().getString(R.string.showing_result);
        TextView result = (TextView) findViewById(R.id.result);
        result.setText(init);
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}