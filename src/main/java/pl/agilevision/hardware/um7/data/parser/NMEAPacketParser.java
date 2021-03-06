package pl.agilevision.hardware.um7.data.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.exception.SuperCsvException;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;
import pl.agilevision.hardware.um7.UM7Attributes;
import pl.agilevision.hardware.um7.callback.DataCallback;
import pl.agilevision.hardware.um7.data.UM7Packet;
import pl.agilevision.hardware.um7.data.attributes.ConfigurableRateAttribute;
import pl.agilevision.hardware.um7.data.attributes.nmea.*;
import pl.agilevision.hardware.um7.data.nmea.UM7NMEAPacket;
import pl.agilevision.hardware.um7.impl.DefaultUM7Client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by volodymyr on 02.12.16.
 */
public class NMEAPacketParser extends PacketParser {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultUM7Client.class);
  private static final byte[] PACKET_PREFIX = "$PCHR".getBytes(StandardCharsets.US_ASCII);
  private static final int PACKET_HEADER_LENGTH = PACKET_PREFIX.length + 2;

  private static final int CHECKSUM_BLOCK_SIZE = 4;
  private static final int RADIX_HEX = 16;

  private static final Map<String, String[]> PACKET_COLUMNS_MAPPING;
  private static final Map<String, CellProcessor[]> PACKET_PROCESSOR_MAPPING;
  private static final Map<String, ConfigurableRateAttribute> PACKET_ATTRIBUTE_MAPPING;

  static {
    PACKET_COLUMNS_MAPPING = new HashMap<>();
    PACKET_COLUMNS_MAPPING.put(NmeaHealth.NMEA_HEADER, NmeaHealth.parseFormat);
    PACKET_COLUMNS_MAPPING.put(NmeaPose.NMEA_HEADER, NmeaPose.parseFormat);
    PACKET_COLUMNS_MAPPING.put(NmeaAttitude.NMEA_HEADER, NmeaAttitude.parseFormat);
    PACKET_COLUMNS_MAPPING.put(NmeaSensor.NMEA_HEADER, NmeaSensor.parseFormat);
    PACKET_COLUMNS_MAPPING.put(NmeaRate.NMEA_HEADER, NmeaRate.parseFormat);
    PACKET_COLUMNS_MAPPING.put(NmeaGpsPose.NMEA_HEADER, NmeaGpsPose.parseFormat);
    PACKET_COLUMNS_MAPPING.put(NmeaQuaternion.NMEA_HEADER, NmeaQuaternion.parseFormat);

    PACKET_PROCESSOR_MAPPING = new HashMap<>();
    PACKET_PROCESSOR_MAPPING.put(NmeaHealth.NMEA_HEADER, NmeaHealth.parseCellProcessor);
    PACKET_PROCESSOR_MAPPING.put(NmeaPose.NMEA_HEADER, NmeaPose.parseCellProcessor);
    PACKET_PROCESSOR_MAPPING.put(NmeaAttitude.NMEA_HEADER, NmeaAttitude.parseCellProcessor);
    PACKET_PROCESSOR_MAPPING.put(NmeaSensor.NMEA_HEADER, NmeaSensor.parseCellProcessor);
    PACKET_PROCESSOR_MAPPING.put(NmeaRate.NMEA_HEADER, NmeaRate.parseCellProcessor);
    PACKET_PROCESSOR_MAPPING.put(NmeaGpsPose.NMEA_HEADER, NmeaGpsPose.parseCellProcessor);
    PACKET_PROCESSOR_MAPPING.put(NmeaQuaternion.NMEA_HEADER, NmeaQuaternion.parseCellProcessor);

    PACKET_ATTRIBUTE_MAPPING = new HashMap<>();
    PACKET_ATTRIBUTE_MAPPING.put(NmeaHealth.NMEA_HEADER, UM7Attributes.NMEA.Health);
    PACKET_ATTRIBUTE_MAPPING.put(NmeaPose.NMEA_HEADER, UM7Attributes.NMEA.Pose);
    PACKET_ATTRIBUTE_MAPPING.put(NmeaAttitude.NMEA_HEADER, UM7Attributes.NMEA.Attitude);
    PACKET_ATTRIBUTE_MAPPING.put(NmeaSensor.NMEA_HEADER, UM7Attributes.NMEA.Sensor);
    PACKET_ATTRIBUTE_MAPPING.put(NmeaRate.NMEA_HEADER, UM7Attributes.NMEA.Rates);
    PACKET_ATTRIBUTE_MAPPING.put(NmeaGpsPose.NMEA_HEADER, UM7Attributes.NMEA.GpsPose);
    PACKET_ATTRIBUTE_MAPPING.put(NmeaQuaternion.NMEA_HEADER, UM7Attributes.NMEA.Quaternion);
  }

  public NMEAPacketParser(){

  }

  public void removeElementFromArray(Object[] a, int del) {
    System.arraycopy(a,del+1,a,del,a.length-1-del);
  }

  private static NMEAPacketParser single;

  public static NMEAPacketParser getParser() {
    if (single == null) {
      single = new NMEAPacketParser();
    }
    return single;
  }

  @Override
  public boolean canParse(byte[] data, Integer... startAddress) {
    return ParserUtils.beginsWith(data, PACKET_PREFIX);
  }


  @Override
  public UM7Packet parse(byte[] data, Map<ConfigurableRateAttribute, DataCallback> callbacks, Integer... startAddress) {
    final String header = new String(Arrays.copyOf(data, PACKET_HEADER_LENGTH - 1),
        StandardCharsets.US_ASCII);

    if (PACKET_COLUMNS_MAPPING.containsKey(header)) {
      final String checksumStr =  new String(Arrays.copyOfRange(data, data.length - CHECKSUM_BLOCK_SIZE + 2, data.length));
      final int checksum = Integer.parseInt(checksumStr, RADIX_HEX);

      final byte[] csvData = Arrays.copyOfRange(data, PACKET_HEADER_LENGTH, data.length - CHECKSUM_BLOCK_SIZE);

      final ICsvMapReader mapReader  = new CsvMapReader(new InputStreamReader(new ByteArrayInputStream(csvData)),
        CsvPreference.STANDARD_PREFERENCE);

      String[] mappings = PACKET_COLUMNS_MAPPING.get(header);
      CellProcessor[] processors = PACKET_PROCESSOR_MAPPING.get(header);

      Map<String, Object> attributesMap;
      try {
        attributesMap = mapReader.read(mappings, processors);
      } catch (IOException|SuperCsvException e) {
        try {
          // according to some firmware versions we should try to parse without last element (GPS Heading angle)
          removeElementFromArray(mappings, mappings.length-1);
          removeElementFromArray(processors, processors.length-1);
          attributesMap = mapReader.read(mappings, processors);

        } catch (IOException|SuperCsvException e1) {
          e1.printStackTrace();
          LOG.warn("exception when parsing NMEA {}, {}", header, data);
          return null;
        }


      }

      UM7NMEAPacket p = new UM7NMEAPacket();
      p.setAttributes(attributesMap);


      int realChecksum = 0;
      boolean calc = false;
      for (byte b: data) {
        if (!calc && b == '$') {
          calc = true;
          continue;
        }
        if (calc && b == '*') {
          calc = false;
        }

        if (calc) {
          realChecksum = realChecksum ^ b;
        }
      }

      if (realChecksum != checksum) {
        LOG.warn("NMEA checksum mismatch real: 0x{}, dest: 0x{}",
          String.format("%2x", realChecksum),
          String.format("%2x", checksum));
        return null;
      }
      this.callBack(callbacks, PACKET_ATTRIBUTE_MAPPING.get(header), p);

      return p;


    } else {
      LOG.warn("Unknown packet header {}", header);
      return null;
    }
  }
}
