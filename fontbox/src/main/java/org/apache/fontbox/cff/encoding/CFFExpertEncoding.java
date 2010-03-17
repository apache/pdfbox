/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.fontbox.cff.encoding;

/**
 * This is specialized CFFEncoding. It's used if the EncodingId of a font is set to 1.
 * 
 * @author Villu Ruusmann
 * @version $Revision$
 */
public class CFFExpertEncoding extends CFFEncoding
{

    private CFFExpertEncoding()
    {
    }

    /**
     * Returns an instance of the CFFExportEncoding class.
     * @return an instance of CFFExportEncoding
     */
    public static CFFExpertEncoding getInstance()
    {
        return CFFExpertEncoding.INSTANCE;
    }

    private static final CFFExpertEncoding INSTANCE = new CFFExpertEncoding();

    static
    {
        INSTANCE.register(0, 0);
        INSTANCE.register(1, 0);
        INSTANCE.register(2, 0);
        INSTANCE.register(3, 0);
        INSTANCE.register(4, 0);
        INSTANCE.register(5, 0);
        INSTANCE.register(6, 0);
        INSTANCE.register(7, 0);
        INSTANCE.register(8, 0);
        INSTANCE.register(9, 0);
        INSTANCE.register(10, 0);
        INSTANCE.register(11, 0);
        INSTANCE.register(12, 0);
        INSTANCE.register(13, 0);
        INSTANCE.register(14, 0);
        INSTANCE.register(15, 0);
        INSTANCE.register(16, 0);
        INSTANCE.register(17, 0);
        INSTANCE.register(18, 0);
        INSTANCE.register(19, 0);
        INSTANCE.register(20, 0);
        INSTANCE.register(21, 0);
        INSTANCE.register(22, 0);
        INSTANCE.register(23, 0);
        INSTANCE.register(24, 0);
        INSTANCE.register(25, 0);
        INSTANCE.register(26, 0);
        INSTANCE.register(27, 0);
        INSTANCE.register(28, 0);
        INSTANCE.register(29, 0);
        INSTANCE.register(30, 0);
        INSTANCE.register(31, 0);
        INSTANCE.register(32, 1);
        INSTANCE.register(33, 229);
        INSTANCE.register(34, 230);
        INSTANCE.register(35, 0);
        INSTANCE.register(36, 231);
        INSTANCE.register(37, 232);
        INSTANCE.register(38, 233);
        INSTANCE.register(39, 234);
        INSTANCE.register(40, 235);
        INSTANCE.register(41, 236);
        INSTANCE.register(42, 237);
        INSTANCE.register(43, 238);
        INSTANCE.register(44, 13);
        INSTANCE.register(45, 14);
        INSTANCE.register(46, 15);
        INSTANCE.register(47, 99);
        INSTANCE.register(48, 239);
        INSTANCE.register(49, 240);
        INSTANCE.register(50, 241);
        INSTANCE.register(51, 242);
        INSTANCE.register(52, 243);
        INSTANCE.register(53, 244);
        INSTANCE.register(54, 245);
        INSTANCE.register(55, 246);
        INSTANCE.register(56, 247);
        INSTANCE.register(57, 248);
        INSTANCE.register(58, 27);
        INSTANCE.register(59, 28);
        INSTANCE.register(60, 249);
        INSTANCE.register(61, 250);
        INSTANCE.register(62, 251);
        INSTANCE.register(63, 252);
        INSTANCE.register(64, 0);
        INSTANCE.register(65, 253);
        INSTANCE.register(66, 254);
        INSTANCE.register(67, 255);
        INSTANCE.register(68, 256);
        INSTANCE.register(69, 257);
        INSTANCE.register(70, 0);
        INSTANCE.register(71, 0);
        INSTANCE.register(72, 0);
        INSTANCE.register(73, 258);
        INSTANCE.register(74, 0);
        INSTANCE.register(75, 0);
        INSTANCE.register(76, 259);
        INSTANCE.register(77, 260);
        INSTANCE.register(78, 261);
        INSTANCE.register(79, 262);
        INSTANCE.register(80, 0);
        INSTANCE.register(81, 0);
        INSTANCE.register(82, 263);
        INSTANCE.register(83, 264);
        INSTANCE.register(84, 265);
        INSTANCE.register(85, 0);
        INSTANCE.register(86, 266);
        INSTANCE.register(87, 109);
        INSTANCE.register(88, 110);
        INSTANCE.register(89, 267);
        INSTANCE.register(90, 268);
        INSTANCE.register(91, 269);
        INSTANCE.register(92, 0);
        INSTANCE.register(93, 270);
        INSTANCE.register(94, 271);
        INSTANCE.register(95, 272);
        INSTANCE.register(96, 273);
        INSTANCE.register(97, 274);
        INSTANCE.register(98, 275);
        INSTANCE.register(99, 276);
        INSTANCE.register(100, 277);
        INSTANCE.register(101, 278);
        INSTANCE.register(102, 279);
        INSTANCE.register(103, 280);
        INSTANCE.register(104, 281);
        INSTANCE.register(105, 282);
        INSTANCE.register(106, 283);
        INSTANCE.register(107, 284);
        INSTANCE.register(108, 285);
        INSTANCE.register(109, 286);
        INSTANCE.register(110, 287);
        INSTANCE.register(111, 288);
        INSTANCE.register(112, 289);
        INSTANCE.register(113, 290);
        INSTANCE.register(114, 291);
        INSTANCE.register(115, 292);
        INSTANCE.register(116, 293);
        INSTANCE.register(117, 294);
        INSTANCE.register(118, 295);
        INSTANCE.register(119, 296);
        INSTANCE.register(120, 297);
        INSTANCE.register(121, 298);
        INSTANCE.register(122, 299);
        INSTANCE.register(123, 300);
        INSTANCE.register(124, 301);
        INSTANCE.register(125, 302);
        INSTANCE.register(126, 303);
        INSTANCE.register(127, 0);
        INSTANCE.register(128, 0);
        INSTANCE.register(129, 0);
        INSTANCE.register(130, 0);
        INSTANCE.register(131, 0);
        INSTANCE.register(132, 0);
        INSTANCE.register(133, 0);
        INSTANCE.register(134, 0);
        INSTANCE.register(135, 0);
        INSTANCE.register(136, 0);
        INSTANCE.register(137, 0);
        INSTANCE.register(138, 0);
        INSTANCE.register(139, 0);
        INSTANCE.register(140, 0);
        INSTANCE.register(141, 0);
        INSTANCE.register(142, 0);
        INSTANCE.register(143, 0);
        INSTANCE.register(144, 0);
        INSTANCE.register(145, 0);
        INSTANCE.register(146, 0);
        INSTANCE.register(147, 0);
        INSTANCE.register(148, 0);
        INSTANCE.register(149, 0);
        INSTANCE.register(150, 0);
        INSTANCE.register(151, 0);
        INSTANCE.register(152, 0);
        INSTANCE.register(153, 0);
        INSTANCE.register(154, 0);
        INSTANCE.register(155, 0);
        INSTANCE.register(156, 0);
        INSTANCE.register(157, 0);
        INSTANCE.register(158, 0);
        INSTANCE.register(159, 0);
        INSTANCE.register(160, 0);
        INSTANCE.register(161, 304);
        INSTANCE.register(162, 305);
        INSTANCE.register(163, 306);
        INSTANCE.register(164, 0);
        INSTANCE.register(165, 0);
        INSTANCE.register(166, 307);
        INSTANCE.register(167, 308);
        INSTANCE.register(168, 309);
        INSTANCE.register(169, 310);
        INSTANCE.register(170, 311);
        INSTANCE.register(171, 0);
        INSTANCE.register(172, 312);
        INSTANCE.register(173, 0);
        INSTANCE.register(174, 0);
        INSTANCE.register(175, 313);
        INSTANCE.register(176, 0);
        INSTANCE.register(177, 0);
        INSTANCE.register(178, 314);
        INSTANCE.register(179, 315);
        INSTANCE.register(180, 0);
        INSTANCE.register(181, 0);
        INSTANCE.register(182, 316);
        INSTANCE.register(183, 317);
        INSTANCE.register(184, 318);
        INSTANCE.register(185, 0);
        INSTANCE.register(186, 0);
        INSTANCE.register(187, 0);
        INSTANCE.register(188, 158);
        INSTANCE.register(189, 155);
        INSTANCE.register(190, 163);
        INSTANCE.register(191, 319);
        INSTANCE.register(192, 320);
        INSTANCE.register(193, 321);
        INSTANCE.register(194, 322);
        INSTANCE.register(195, 323);
        INSTANCE.register(196, 324);
        INSTANCE.register(197, 325);
        INSTANCE.register(198, 0);
        INSTANCE.register(199, 0);
        INSTANCE.register(200, 326);
        INSTANCE.register(201, 150);
        INSTANCE.register(202, 164);
        INSTANCE.register(203, 169);
        INSTANCE.register(204, 327);
        INSTANCE.register(205, 328);
        INSTANCE.register(206, 329);
        INSTANCE.register(207, 330);
        INSTANCE.register(208, 331);
        INSTANCE.register(209, 332);
        INSTANCE.register(210, 333);
        INSTANCE.register(211, 334);
        INSTANCE.register(212, 335);
        INSTANCE.register(213, 336);
        INSTANCE.register(214, 337);
        INSTANCE.register(215, 338);
        INSTANCE.register(216, 339);
        INSTANCE.register(217, 340);
        INSTANCE.register(218, 341);
        INSTANCE.register(219, 342);
        INSTANCE.register(220, 343);
        INSTANCE.register(221, 344);
        INSTANCE.register(222, 345);
        INSTANCE.register(223, 346);
        INSTANCE.register(224, 347);
        INSTANCE.register(225, 348);
        INSTANCE.register(226, 349);
        INSTANCE.register(227, 350);
        INSTANCE.register(228, 351);
        INSTANCE.register(229, 352);
        INSTANCE.register(230, 353);
        INSTANCE.register(231, 354);
        INSTANCE.register(232, 355);
        INSTANCE.register(233, 356);
        INSTANCE.register(234, 357);
        INSTANCE.register(235, 358);
        INSTANCE.register(236, 359);
        INSTANCE.register(237, 360);
        INSTANCE.register(238, 361);
        INSTANCE.register(239, 362);
        INSTANCE.register(240, 363);
        INSTANCE.register(241, 364);
        INSTANCE.register(242, 365);
        INSTANCE.register(243, 366);
        INSTANCE.register(244, 367);
        INSTANCE.register(245, 368);
        INSTANCE.register(246, 369);
        INSTANCE.register(247, 370);
        INSTANCE.register(248, 371);
        INSTANCE.register(249, 372);
        INSTANCE.register(250, 373);
        INSTANCE.register(251, 374);
        INSTANCE.register(252, 375);
        INSTANCE.register(253, 376);
        INSTANCE.register(254, 377);
        INSTANCE.register(255, 378);
    }
}