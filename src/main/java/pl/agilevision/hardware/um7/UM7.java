package pl.agilevision.hardware.um7;

import pl.agilevision.hardware.um7.data.UM7DataSample;
import pl.agilevision.hardware.um7.exceptions.DeviceConnectionException;
import pl.agilevision.hardware.um7.exceptions.OperationTimeoutException;

import java.io.IOException;

/**
 * Interface to interact with the UM7 device
 * @author Volodymyr Rudyi (volodymyr@agilevision.pl)
 */
public interface UM7 {

  /**
   * Sends request to zero gyros and waits for confirmation from sensor
   * @return true if the operation was successful,false in a case of a failure
   * @throws DeviceConnectionException in a case of the failure while communicating with the device
   * @throws OperationTimeoutException if the timeout passed before the operation finished
   */
  boolean zeroGyros() throws DeviceConnectionException, OperationTimeoutException;

  /**
   * Sends request to reset ekf and waits for confirmation from sensor
   * @return true if the operation was successful,false in a case of a failure
   * @throws DeviceConnectionException in a case of the failure while communicating with the device
   * @throws OperationTimeoutException if the timeout passed before the operation finished
   */
  boolean resetEkf() throws DeviceConnectionException, OperationTimeoutException;

  /**
   * Sends request to perform a factory reset and waits for confirmation from sensor
   * @return true if the operation was successful,false in a case of a failure
   * @throws DeviceConnectionException in a case of the failure while communicating with the device
   * @throws OperationTimeoutException if the timeout passed before the operation finished
   */
  boolean resetToFactory()  throws DeviceConnectionException, OperationTimeoutException;

  /**
   *
   * @return true if the operation was successful,false in a case of a failure
   * @throws DeviceConnectionException in a case of the failure while communicating with the device
   * @throws OperationTimeoutException if the timeout passed before the operation finished
   */
  boolean setMagReference()  throws DeviceConnectionException, OperationTimeoutException;

  /**
   *
   * @return true if the operation was successful,false in a case of a failure
   * @throws DeviceConnectionException in a case of the failure while communicating with the device
   * @throws OperationTimeoutException if the timeout passed before the operation finished
   */
  boolean setHomePosition()  throws DeviceConnectionException, OperationTimeoutException;

  /**
   *
   * @return true if the operation was successful,false in a case of a failure
   * @throws DeviceConnectionException in a case of the failure while communicating with the device
   * @throws OperationTimeoutException if the timeout passed before the operation finished
   */
  boolean flashCommit()  throws DeviceConnectionException, OperationTimeoutException;

  /**
   *
   * @return true if the operation was successful,false in a case of a failure
   * @throws DeviceConnectionException in a case of the failure while communicating with the device
   * @throws OperationTimeoutException if the timeout passed before the operation finished
   */
  String getFirmwareVersion()  throws DeviceConnectionException, OperationTimeoutException;

  /**
   * Reads a data sample
   * @return data sample
   * @throws DeviceConnectionException in a case of the failure while communicating with the device
   * @throws OperationTimeoutException if the timeout passed before the operation finished
   */
  UM7DataSample readState() throws DeviceConnectionException, OperationTimeoutException;


  UM7DataSample getState();

  boolean catchAllSamples(final String [] wantedState, float timeout) throws DeviceConnectionException, IOException;
}
