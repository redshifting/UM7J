package pl.agilevision.hardware.um7.data;

/**
 * Rate configuration
 *
 * @author Ivan Borschov (iborschov@agilevision.pl)
 * @author Volodymyr Rudyi (volodymyr@agilevision.pl)
 */
public class AcceleratorRaw extends GyroRaw {
  public static String X = "accel_raw_x";
  public static String Y = "accel_raw_y";
  public static String Z = "accel_raw_z";
  public static String Time = "accel_raw_time";

  public AcceleratorRaw(int registerAddress, int bitOffset) {
    super(registerAddress, bitOffset);
  }
}