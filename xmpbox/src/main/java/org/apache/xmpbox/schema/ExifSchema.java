/*****************************************************************************
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 ****************************************************************************/

package org.apache.xmpbox.schema;

import org.apache.xmpbox.XMPMetadata;

import java.util.List;
import org.apache.xmpbox.type.ArrayProperty;
import org.apache.xmpbox.type.Cardinality;
import org.apache.xmpbox.type.PropertyType;
import org.apache.xmpbox.type.StructuredType;
import org.apache.xmpbox.type.Types;

/**
 * Representation of a Exif Schema
 *
 */

@StructuredType(preferedPrefix = "exif", namespace = "http://ns.adobe.com/exif/1.0/")
public class ExifSchema extends XMPSchema
{

    @PropertyType(type = Types.LangAlt, card = Cardinality.Simple)
    public static final String USER_COMMENT = "UserComment";

    @PropertyType(type=Types.Text, card= Cardinality.Simple)
    public static final String EXIF_VERSION = "ExifVersion";

    @PropertyType(type=Types.Text, card= Cardinality.Simple)
    public static final String FLASH_PIX_VERSION = "FlashpixVersion";

    @PropertyType(type=Types.Integer, card= Cardinality.Simple)
    public static final String COLOR_SPACE = "ColorSpace";

    @PropertyType(type=Types.Integer, card= Cardinality.Seq)
    public static final String COMPONENTS_CONFIGURATION = "ComponentsConfiguration";

    @PropertyType(type=Types.Rational, card= Cardinality.Simple)
    public static final String COMPRESSED_BPP = "CompressedBitsPerPixel";

    @PropertyType(type=Types.Integer, card= Cardinality.Simple)
    public static final String PIXEL_X_DIMENSION = "PixelXDimension";

    @PropertyType(type=Types.Integer, card= Cardinality.Simple)
    public static final String PIXEL_Y_DIMENSION = "PixelYDimension";

    @PropertyType(type=Types.Text, card= Cardinality.Simple)
    public static final String RELATED_SOUND_FILE = "RelatedSoundFile";

    @PropertyType(type=Types.Date, card= Cardinality.Simple)
    public static final String DATE_TIME_ORIGINAL = "DateTimeOriginal";

    @PropertyType(type=Types.Date, card= Cardinality.Simple)
    public static final String DATE_TIME_DIGITIZED = "DateTimeDigitized";

    @PropertyType(type=Types.Rational, card= Cardinality.Simple)
    public static final String EXPOSURE_TIME = "ExposureTime";

    @PropertyType(type=Types.Rational, card= Cardinality.Simple)
    public static final String F_NUMBER = "FNumber";

    @PropertyType(type=Types.Integer, card= Cardinality.Simple)
    public static final String EXPOSURE_PROGRAM = "ExposureProgram";

    @PropertyType(type=Types.Text, card= Cardinality.Simple)
    public static final String SPECTRAL_SENSITIVITY = "SpectralSensitivity";

    @PropertyType(type=Types.Integer, card= Cardinality.Seq)
    public static final String ISO_SPEED_RATINGS = "ISOSpeedRatings";

    @PropertyType(type=Types.Rational, card= Cardinality.Simple)
    public static final String SHUTTER_SPEED_VALUE = "ShutterSpeedValue";

    @PropertyType(type=Types.Rational, card= Cardinality.Simple)
    public static final String APERTURE_VALUE = "ApertureValue";

    @PropertyType(type=Types.Rational, card= Cardinality.Simple)
    public static final String BRIGHTNESS_VALUE = "BrightnessValue";

    @PropertyType(type=Types.Rational, card= Cardinality.Simple)
    public static final String EXPOSURE_BIAS_VALUE = "ExposureBiasValue";

    @PropertyType(type=Types.Rational, card= Cardinality.Simple)
    public static final String MAX_APERTURE_VALUE = "MaxApertureValue";

    @PropertyType(type=Types.Rational, card= Cardinality.Simple)
    public static final String SUBJECT_DISTANCE = "SubjectDistance";

    @PropertyType(type=Types.Integer, card= Cardinality.Simple)
    public static final String METERING_MODE = "MeteringMode";

    @PropertyType(type=Types.Integer, card= Cardinality.Simple)
    public static final String LIGHT_SOURCE = "LightSource";

    @PropertyType(type=Types.Rational, card= Cardinality.Simple)
    public static final String FLASH_ENERGY = "FlashEnergy";

    @PropertyType(type=Types.Rational, card= Cardinality.Simple)
    public static final String FOCAL_LENGTH = "FocalLength";

    @PropertyType(type=Types.Rational, card= Cardinality.Simple)
    public static final String FOCAL_PLANE_XRESOLUTION = "FocalPlaneXResolution";

    @PropertyType(type=Types.Rational, card= Cardinality.Simple)
    public static final String FOCAL_PLANE_YRESOLUTION = "FocalPlaneYResolution";

    @PropertyType(type=Types.Integer, card= Cardinality.Seq)
    public static final String SUBJECT_AREA = "SubjectArea";

    @PropertyType(type=Types.Integer, card= Cardinality.Simple)
    public static final String FOCAL_PLANE_RESOLUTION_UNIT = "FocalPlaneResolutionUnit";

    @PropertyType(type=Types.Integer, card= Cardinality.Seq)
    public static final String SUBJECT_LOCATION = "SubjectLocation";

    @PropertyType(type=Types.Rational, card= Cardinality.Simple)
    public static final String EXPOSURE_INDEX = "ExposureIndex";

    @PropertyType(type=Types.Integer, card= Cardinality.Simple)
    public static final String SENSING_METHOD = "SensingMethod";

    @PropertyType(type=Types.Integer, card= Cardinality.Simple)
    public static final String FILE_SOURCE = "FileSource";

    @PropertyType(type=Types.Integer, card= Cardinality.Simple)
    public static final String SCENE_TYPE = "SceneType";

    @PropertyType(type=Types.Integer, card= Cardinality.Simple)
    public static final String CUSTOM_RENDERED = "CustomRendered";

    @PropertyType(type=Types.Integer, card= Cardinality.Simple)
    public static final String WHITE_BALANCE = "WhiteBalance";

    @PropertyType(type=Types.Integer, card= Cardinality.Simple)
    public static final String EXPOSURE_MODE = "ExposureMode";

    @PropertyType(type=Types.Rational, card= Cardinality.Simple)
    public static final String DIGITAL_ZOOM_RATIO = "DigitalZoomRatio";

    @PropertyType(type=Types.Integer, card= Cardinality.Simple)
    public static final String FOCAL_LENGTH_IN_3_5MM_FILM = "FocalLengthIn35mmFilm";

    @PropertyType(type=Types.Integer, card= Cardinality.Simple)
    public static final String SCENE_CAPTURE_TYPE = "SceneCaptureType";

    @PropertyType(type=Types.Integer, card= Cardinality.Simple)
    public static final String GAIN_CONTROL = "GainControl";

    @PropertyType(type=Types.Integer, card= Cardinality.Simple)
    public static final String CONTRAST = "Contrast";

    @PropertyType(type=Types.Integer, card= Cardinality.Simple)
    public static final String SATURATION = "Saturation";

    @PropertyType(type=Types.Integer, card= Cardinality.Simple)
    public static final String SHARPNESS = "Sharpness";

    @PropertyType(type=Types.Integer, card= Cardinality.Simple)
    public static final String SUBJECT_DISTANCE_RANGE = "SubjectDistanceRange";

    @PropertyType(type=Types.Text, card= Cardinality.Simple)
    public static final String IMAGE_UNIQUE_ID = "ImageUniqueID";

    @PropertyType(type=Types.Text, card= Cardinality.Simple)
    public static final String GPSVERSION_ID = "GPSVersionID";

    @PropertyType(type=Types.Text, card= Cardinality.Simple)
    public static final String GPS_SATELLITES = "GPSSatellites";

    @PropertyType(type=Types.Text, card= Cardinality.Simple)
    public static final String GPS_STATUS = "GPSStatus";

    @PropertyType(type=Types.Text, card= Cardinality.Simple)
    public static final String GPS_MEASURE_MODE = "GPSMeasureMode";

    @PropertyType(type=Types.Text, card= Cardinality.Simple)
    public static final String GPS_MAP_DATUM = "GPSMapDatum";

    @PropertyType(type=Types.Text, card= Cardinality.Simple)
    public static final String GPS_SPEED_REF = "GPSSpeedRef";

    @PropertyType(type=Types.Text, card= Cardinality.Simple)
    public static final String GPS_TRACK_REF = "GPSTrackRef";

    @PropertyType(type=Types.Text, card= Cardinality.Simple)
    public static final String GPS_IMG_DIRECTION_REF = "GPSImgDirectionRef";

    @PropertyType(type=Types.Text, card= Cardinality.Simple)
    public static final String GPS_DEST_BEARING_REF = "GPSDestBearingRef";

    @PropertyType(type=Types.Text, card= Cardinality.Simple)
    public static final String GPS_DEST_DISTANCE_REF = "GPSDestDistanceRef";

    @PropertyType(type=Types.Text, card= Cardinality.Simple)
    public static final String GPS_PROCESSING_METHOD = "GPSProcessingMethod";

    @PropertyType(type=Types.Text, card= Cardinality.Simple)
    public static final String GPS_AREA_INFORMATION = "GPSAreaInformation";

    @PropertyType(type=Types.Rational, card= Cardinality.Simple)
    public static final String GPS_ALTITUDE = "GPSAltitude";

    @PropertyType(type=Types.Rational, card= Cardinality.Simple)
    public static final String GPS_DOP = "GPSDOP";

    @PropertyType(type=Types.Rational, card= Cardinality.Simple)
    public static final String GPS_SPEED = "GPSSpeed";

    @PropertyType(type=Types.Rational, card= Cardinality.Simple)
    public static final String GPS_TRACK = "GPSTrack";

    @PropertyType(type=Types.Rational, card= Cardinality.Simple)
    public static final String GPS_IMG_DIRECTION = "GPSImgDirection";

    @PropertyType(type=Types.Rational, card= Cardinality.Simple)
    public static final String GPS_DEST_BEARING = "GPSDestBearing";

    @PropertyType(type=Types.Rational, card= Cardinality.Simple)
    public static final String GPS_DEST_DISTANCE = "GPSDestDistance";

    @PropertyType(type=Types.Integer, card= Cardinality.Simple)
    public static final String GPS_ALTITUDE_REF = "GPSAltitudeRef";

    @PropertyType(type=Types.Integer, card= Cardinality.Simple)
    public static final String GPS_DIFFERENTIAL = "GPSDifferential";

    @PropertyType(type=Types.Date, card= Cardinality.Simple)
    public static final String GPS_TIME_STAMP = "GPSTimeStamp";

    @PropertyType(type=Types.OECF)
    public static final String OECF = "OECF";

    @PropertyType(type=Types.OECF)
    public static final String SPATIAL_FREQUENCY_RESPONSE = "SpatialFrequencyResponse";

    @PropertyType(type=Types.GPSCoordinate)
    public static final String GPS_LATITUDE = "GPSLatitude";

    @PropertyType(type=Types.GPSCoordinate)
    public static final String GPS_LONGITUDE = "GPSLongitude";

    @PropertyType(type=Types.GPSCoordinate)
    public static final String GPS_DEST_LATITUDE = "GPSDestLatitude";

    @PropertyType(type=Types.GPSCoordinate)
    public static final String GPS_DEST_LONGITUDE = "GPSDestLongitude";

    @PropertyType(type = Types.CFAPattern)
    public static final String CFA_PATTERN = "CFAPattern";

    @PropertyType(type = Types.Flash)
    public static final String FLASH = "Flash";

    @PropertyType(type = Types.CFAPattern)
    public static final String CFA_PATTERN_TYPE = "CFAPatternType";

    @PropertyType(type = Types.DeviceSettings)
    public static final String DEVICE_SETTING_DESCRIPTION = "DeviceSettingDescription";

    public ExifSchema(XMPMetadata metadata)
    {
        super(metadata);
    }

    public ExifSchema(XMPMetadata metadata, String ownPrefix)
    {
        super(metadata, ownPrefix);
    }


    /**
     * Return the Lang Alt UserComment property
     *
     * @return user comment property
     */
    public ArrayProperty getUserCommentProperty()
    {
        return (ArrayProperty) getProperty(USER_COMMENT);
    }

    /**
     * Return a list of languages defined in UserComment property
     *
     * @return list of UserComment languages values defined
     */
    public List<String> getUserCommentLanguages()
    {
        return getUnqualifiedLanguagePropertyLanguagesValue(USER_COMMENT);
    }

    /**
     * Return a language value for UserComment property
     *
     * @param lang
     *            language concerned
     * @return the UserComment value for specified language
     */
    public String getUserComment(String lang)
    {
        return getUnqualifiedLanguagePropertyValue(USER_COMMENT, lang);
    }

    /**
     * Return the default value for UserComment property
     *
     * @see ExifSchema#getUserComment(String)
     */
    public String getUserComment()
    {
        return getUserComment(null);
    }


}
