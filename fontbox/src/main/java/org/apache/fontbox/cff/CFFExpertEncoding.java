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
package org.apache.fontbox.cff;

/**
 * This is specialized CFFEncoding. It's used if the EncodingId of a font is set to 1.
 * 
 * @author Villu Ruusmann
 */
public final class CFFExpertEncoding extends CFFEncoding
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
        INSTANCE.add(0, 0);
        INSTANCE.add(1, 0);
        INSTANCE.add(2, 0);
        INSTANCE.add(3, 0);
        INSTANCE.add(4, 0);
        INSTANCE.add(5, 0);
        INSTANCE.add(6, 0);
        INSTANCE.add(7, 0);
        INSTANCE.add(8, 0);
        INSTANCE.add(9, 0);
        INSTANCE.add(10, 0);
        INSTANCE.add(11, 0);
        INSTANCE.add(12, 0);
        INSTANCE.add(13, 0);
        INSTANCE.add(14, 0);
        INSTANCE.add(15, 0);
        INSTANCE.add(16, 0);
        INSTANCE.add(17, 0);
        INSTANCE.add(18, 0);
        INSTANCE.add(19, 0);
        INSTANCE.add(20, 0);
        INSTANCE.add(21, 0);
        INSTANCE.add(22, 0);
        INSTANCE.add(23, 0);
        INSTANCE.add(24, 0);
        INSTANCE.add(25, 0);
        INSTANCE.add(26, 0);
        INSTANCE.add(27, 0);
        INSTANCE.add(28, 0);
        INSTANCE.add(29, 0);
        INSTANCE.add(30, 0);
        INSTANCE.add(31, 0);
        INSTANCE.add(32, 1);
        INSTANCE.add(33, 229);
        INSTANCE.add(34, 230);
        INSTANCE.add(35, 0);
        INSTANCE.add(36, 231);
        INSTANCE.add(37, 232);
        INSTANCE.add(38, 233);
        INSTANCE.add(39, 234);
        INSTANCE.add(40, 235);
        INSTANCE.add(41, 236);
        INSTANCE.add(42, 237);
        INSTANCE.add(43, 238);
        INSTANCE.add(44, 13);
        INSTANCE.add(45, 14);
        INSTANCE.add(46, 15);
        INSTANCE.add(47, 99);
        INSTANCE.add(48, 239);
        INSTANCE.add(49, 240);
        INSTANCE.add(50, 241);
        INSTANCE.add(51, 242);
        INSTANCE.add(52, 243);
        INSTANCE.add(53, 244);
        INSTANCE.add(54, 245);
        INSTANCE.add(55, 246);
        INSTANCE.add(56, 247);
        INSTANCE.add(57, 248);
        INSTANCE.add(58, 27);
        INSTANCE.add(59, 28);
        INSTANCE.add(60, 249);
        INSTANCE.add(61, 250);
        INSTANCE.add(62, 251);
        INSTANCE.add(63, 252);
        INSTANCE.add(64, 0);
        INSTANCE.add(65, 253);
        INSTANCE.add(66, 254);
        INSTANCE.add(67, 255);
        INSTANCE.add(68, 256);
        INSTANCE.add(69, 257);
        INSTANCE.add(70, 0);
        INSTANCE.add(71, 0);
        INSTANCE.add(72, 0);
        INSTANCE.add(73, 258);
        INSTANCE.add(74, 0);
        INSTANCE.add(75, 0);
        INSTANCE.add(76, 259);
        INSTANCE.add(77, 260);
        INSTANCE.add(78, 261);
        INSTANCE.add(79, 262);
        INSTANCE.add(80, 0);
        INSTANCE.add(81, 0);
        INSTANCE.add(82, 263);
        INSTANCE.add(83, 264);
        INSTANCE.add(84, 265);
        INSTANCE.add(85, 0);
        INSTANCE.add(86, 266);
        INSTANCE.add(87, 109);
        INSTANCE.add(88, 110);
        INSTANCE.add(89, 267);
        INSTANCE.add(90, 268);
        INSTANCE.add(91, 269);
        INSTANCE.add(92, 0);
        INSTANCE.add(93, 270);
        INSTANCE.add(94, 271);
        INSTANCE.add(95, 272);
        INSTANCE.add(96, 273);
        INSTANCE.add(97, 274);
        INSTANCE.add(98, 275);
        INSTANCE.add(99, 276);
        INSTANCE.add(100, 277);
        INSTANCE.add(101, 278);
        INSTANCE.add(102, 279);
        INSTANCE.add(103, 280);
        INSTANCE.add(104, 281);
        INSTANCE.add(105, 282);
        INSTANCE.add(106, 283);
        INSTANCE.add(107, 284);
        INSTANCE.add(108, 285);
        INSTANCE.add(109, 286);
        INSTANCE.add(110, 287);
        INSTANCE.add(111, 288);
        INSTANCE.add(112, 289);
        INSTANCE.add(113, 290);
        INSTANCE.add(114, 291);
        INSTANCE.add(115, 292);
        INSTANCE.add(116, 293);
        INSTANCE.add(117, 294);
        INSTANCE.add(118, 295);
        INSTANCE.add(119, 296);
        INSTANCE.add(120, 297);
        INSTANCE.add(121, 298);
        INSTANCE.add(122, 299);
        INSTANCE.add(123, 300);
        INSTANCE.add(124, 301);
        INSTANCE.add(125, 302);
        INSTANCE.add(126, 303);
        INSTANCE.add(127, 0);
        INSTANCE.add(128, 0);
        INSTANCE.add(129, 0);
        INSTANCE.add(130, 0);
        INSTANCE.add(131, 0);
        INSTANCE.add(132, 0);
        INSTANCE.add(133, 0);
        INSTANCE.add(134, 0);
        INSTANCE.add(135, 0);
        INSTANCE.add(136, 0);
        INSTANCE.add(137, 0);
        INSTANCE.add(138, 0);
        INSTANCE.add(139, 0);
        INSTANCE.add(140, 0);
        INSTANCE.add(141, 0);
        INSTANCE.add(142, 0);
        INSTANCE.add(143, 0);
        INSTANCE.add(144, 0);
        INSTANCE.add(145, 0);
        INSTANCE.add(146, 0);
        INSTANCE.add(147, 0);
        INSTANCE.add(148, 0);
        INSTANCE.add(149, 0);
        INSTANCE.add(150, 0);
        INSTANCE.add(151, 0);
        INSTANCE.add(152, 0);
        INSTANCE.add(153, 0);
        INSTANCE.add(154, 0);
        INSTANCE.add(155, 0);
        INSTANCE.add(156, 0);
        INSTANCE.add(157, 0);
        INSTANCE.add(158, 0);
        INSTANCE.add(159, 0);
        INSTANCE.add(160, 0);
        INSTANCE.add(161, 304);
        INSTANCE.add(162, 305);
        INSTANCE.add(163, 306);
        INSTANCE.add(164, 0);
        INSTANCE.add(165, 0);
        INSTANCE.add(166, 307);
        INSTANCE.add(167, 308);
        INSTANCE.add(168, 309);
        INSTANCE.add(169, 310);
        INSTANCE.add(170, 311);
        INSTANCE.add(171, 0);
        INSTANCE.add(172, 312);
        INSTANCE.add(173, 0);
        INSTANCE.add(174, 0);
        INSTANCE.add(175, 313);
        INSTANCE.add(176, 0);
        INSTANCE.add(177, 0);
        INSTANCE.add(178, 314);
        INSTANCE.add(179, 315);
        INSTANCE.add(180, 0);
        INSTANCE.add(181, 0);
        INSTANCE.add(182, 316);
        INSTANCE.add(183, 317);
        INSTANCE.add(184, 318);
        INSTANCE.add(185, 0);
        INSTANCE.add(186, 0);
        INSTANCE.add(187, 0);
        INSTANCE.add(188, 158);
        INSTANCE.add(189, 155);
        INSTANCE.add(190, 163);
        INSTANCE.add(191, 319);
        INSTANCE.add(192, 320);
        INSTANCE.add(193, 321);
        INSTANCE.add(194, 322);
        INSTANCE.add(195, 323);
        INSTANCE.add(196, 324);
        INSTANCE.add(197, 325);
        INSTANCE.add(198, 0);
        INSTANCE.add(199, 0);
        INSTANCE.add(200, 326);
        INSTANCE.add(201, 150);
        INSTANCE.add(202, 164);
        INSTANCE.add(203, 169);
        INSTANCE.add(204, 327);
        INSTANCE.add(205, 328);
        INSTANCE.add(206, 329);
        INSTANCE.add(207, 330);
        INSTANCE.add(208, 331);
        INSTANCE.add(209, 332);
        INSTANCE.add(210, 333);
        INSTANCE.add(211, 334);
        INSTANCE.add(212, 335);
        INSTANCE.add(213, 336);
        INSTANCE.add(214, 337);
        INSTANCE.add(215, 338);
        INSTANCE.add(216, 339);
        INSTANCE.add(217, 340);
        INSTANCE.add(218, 341);
        INSTANCE.add(219, 342);
        INSTANCE.add(220, 343);
        INSTANCE.add(221, 344);
        INSTANCE.add(222, 345);
        INSTANCE.add(223, 346);
        INSTANCE.add(224, 347);
        INSTANCE.add(225, 348);
        INSTANCE.add(226, 349);
        INSTANCE.add(227, 350);
        INSTANCE.add(228, 351);
        INSTANCE.add(229, 352);
        INSTANCE.add(230, 353);
        INSTANCE.add(231, 354);
        INSTANCE.add(232, 355);
        INSTANCE.add(233, 356);
        INSTANCE.add(234, 357);
        INSTANCE.add(235, 358);
        INSTANCE.add(236, 359);
        INSTANCE.add(237, 360);
        INSTANCE.add(238, 361);
        INSTANCE.add(239, 362);
        INSTANCE.add(240, 363);
        INSTANCE.add(241, 364);
        INSTANCE.add(242, 365);
        INSTANCE.add(243, 366);
        INSTANCE.add(244, 367);
        INSTANCE.add(245, 368);
        INSTANCE.add(246, 369);
        INSTANCE.add(247, 370);
        INSTANCE.add(248, 371);
        INSTANCE.add(249, 372);
        INSTANCE.add(250, 373);
        INSTANCE.add(251, 374);
        INSTANCE.add(252, 375);
        INSTANCE.add(253, 376);
        INSTANCE.add(254, 377);
        INSTANCE.add(255, 378);
    }
}