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
 * This is specialized CFFEncoding. It's used if the EncodingId of a font is set to 0.
 * 
 * @author Villu Ruusmann
 * @version $Revision$
 */
public class CFFStandardEncoding extends CFFEncoding
{

    private CFFStandardEncoding()
    {
    }

    /**
     * Returns an instance of the CFFStandardEncoding class.
     * @return an instance of CFFStandardEncoding
     */
    public static CFFStandardEncoding getInstance()
    {
        return CFFStandardEncoding.INSTANCE;
    }

    private static final CFFStandardEncoding INSTANCE = new CFFStandardEncoding();

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
        INSTANCE.register(33, 2);
        INSTANCE.register(34, 3);
        INSTANCE.register(35, 4);
        INSTANCE.register(36, 5);
        INSTANCE.register(37, 6);
        INSTANCE.register(38, 7);
        INSTANCE.register(39, 8);
        INSTANCE.register(40, 9);
        INSTANCE.register(41, 10);
        INSTANCE.register(42, 11);
        INSTANCE.register(43, 12);
        INSTANCE.register(44, 13);
        INSTANCE.register(45, 14);
        INSTANCE.register(46, 15);
        INSTANCE.register(47, 16);
        INSTANCE.register(48, 17);
        INSTANCE.register(49, 18);
        INSTANCE.register(50, 19);
        INSTANCE.register(51, 20);
        INSTANCE.register(52, 21);
        INSTANCE.register(53, 22);
        INSTANCE.register(54, 23);
        INSTANCE.register(55, 24);
        INSTANCE.register(56, 25);
        INSTANCE.register(57, 26);
        INSTANCE.register(58, 27);
        INSTANCE.register(59, 28);
        INSTANCE.register(60, 29);
        INSTANCE.register(61, 30);
        INSTANCE.register(62, 31);
        INSTANCE.register(63, 32);
        INSTANCE.register(64, 33);
        INSTANCE.register(65, 34);
        INSTANCE.register(66, 35);
        INSTANCE.register(67, 36);
        INSTANCE.register(68, 37);
        INSTANCE.register(69, 38);
        INSTANCE.register(70, 39);
        INSTANCE.register(71, 40);
        INSTANCE.register(72, 41);
        INSTANCE.register(73, 42);
        INSTANCE.register(74, 43);
        INSTANCE.register(75, 44);
        INSTANCE.register(76, 45);
        INSTANCE.register(77, 46);
        INSTANCE.register(78, 47);
        INSTANCE.register(79, 48);
        INSTANCE.register(80, 49);
        INSTANCE.register(81, 50);
        INSTANCE.register(82, 51);
        INSTANCE.register(83, 52);
        INSTANCE.register(84, 53);
        INSTANCE.register(85, 54);
        INSTANCE.register(86, 55);
        INSTANCE.register(87, 56);
        INSTANCE.register(88, 57);
        INSTANCE.register(89, 58);
        INSTANCE.register(90, 59);
        INSTANCE.register(91, 60);
        INSTANCE.register(92, 61);
        INSTANCE.register(93, 62);
        INSTANCE.register(94, 63);
        INSTANCE.register(95, 64);
        INSTANCE.register(96, 65);
        INSTANCE.register(97, 66);
        INSTANCE.register(98, 67);
        INSTANCE.register(99, 68);
        INSTANCE.register(100, 69);
        INSTANCE.register(101, 70);
        INSTANCE.register(102, 71);
        INSTANCE.register(103, 72);
        INSTANCE.register(104, 73);
        INSTANCE.register(105, 74);
        INSTANCE.register(106, 75);
        INSTANCE.register(107, 76);
        INSTANCE.register(108, 77);
        INSTANCE.register(109, 78);
        INSTANCE.register(110, 79);
        INSTANCE.register(111, 80);
        INSTANCE.register(112, 81);
        INSTANCE.register(113, 82);
        INSTANCE.register(114, 83);
        INSTANCE.register(115, 84);
        INSTANCE.register(116, 85);
        INSTANCE.register(117, 86);
        INSTANCE.register(118, 87);
        INSTANCE.register(119, 88);
        INSTANCE.register(120, 89);
        INSTANCE.register(121, 90);
        INSTANCE.register(122, 91);
        INSTANCE.register(123, 92);
        INSTANCE.register(124, 93);
        INSTANCE.register(125, 94);
        INSTANCE.register(126, 95);
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
        INSTANCE.register(161, 96);
        INSTANCE.register(162, 97);
        INSTANCE.register(163, 98);
        INSTANCE.register(164, 99);
        INSTANCE.register(165, 100);
        INSTANCE.register(166, 101);
        INSTANCE.register(167, 102);
        INSTANCE.register(168, 103);
        INSTANCE.register(169, 104);
        INSTANCE.register(170, 105);
        INSTANCE.register(171, 106);
        INSTANCE.register(172, 107);
        INSTANCE.register(173, 108);
        INSTANCE.register(174, 109);
        INSTANCE.register(175, 110);
        INSTANCE.register(176, 0);
        INSTANCE.register(177, 111);
        INSTANCE.register(178, 112);
        INSTANCE.register(179, 113);
        INSTANCE.register(180, 114);
        INSTANCE.register(181, 0);
        INSTANCE.register(182, 115);
        INSTANCE.register(183, 116);
        INSTANCE.register(184, 117);
        INSTANCE.register(185, 118);
        INSTANCE.register(186, 119);
        INSTANCE.register(187, 120);
        INSTANCE.register(188, 121);
        INSTANCE.register(189, 122);
        INSTANCE.register(190, 0);
        INSTANCE.register(191, 123);
        INSTANCE.register(192, 0);
        INSTANCE.register(193, 124);
        INSTANCE.register(194, 125);
        INSTANCE.register(195, 126);
        INSTANCE.register(196, 127);
        INSTANCE.register(197, 128);
        INSTANCE.register(198, 129);
        INSTANCE.register(199, 130);
        INSTANCE.register(200, 131);
        INSTANCE.register(201, 0);
        INSTANCE.register(202, 132);
        INSTANCE.register(203, 133);
        INSTANCE.register(204, 0);
        INSTANCE.register(205, 134);
        INSTANCE.register(206, 135);
        INSTANCE.register(207, 136);
        INSTANCE.register(208, 137);
        INSTANCE.register(209, 0);
        INSTANCE.register(210, 0);
        INSTANCE.register(211, 0);
        INSTANCE.register(212, 0);
        INSTANCE.register(213, 0);
        INSTANCE.register(214, 0);
        INSTANCE.register(215, 0);
        INSTANCE.register(216, 0);
        INSTANCE.register(217, 0);
        INSTANCE.register(218, 0);
        INSTANCE.register(219, 0);
        INSTANCE.register(220, 0);
        INSTANCE.register(221, 0);
        INSTANCE.register(222, 0);
        INSTANCE.register(223, 0);
        INSTANCE.register(224, 0);
        INSTANCE.register(225, 138);
        INSTANCE.register(226, 0);
        INSTANCE.register(227, 139);
        INSTANCE.register(228, 0);
        INSTANCE.register(229, 0);
        INSTANCE.register(230, 0);
        INSTANCE.register(231, 0);
        INSTANCE.register(232, 140);
        INSTANCE.register(233, 141);
        INSTANCE.register(234, 142);
        INSTANCE.register(235, 143);
        INSTANCE.register(236, 0);
        INSTANCE.register(237, 0);
        INSTANCE.register(238, 0);
        INSTANCE.register(239, 0);
        INSTANCE.register(240, 0);
        INSTANCE.register(241, 144);
        INSTANCE.register(242, 0);
        INSTANCE.register(243, 0);
        INSTANCE.register(244, 0);
        INSTANCE.register(245, 145);
        INSTANCE.register(246, 0);
        INSTANCE.register(247, 0);
        INSTANCE.register(248, 146);
        INSTANCE.register(249, 147);
        INSTANCE.register(250, 148);
        INSTANCE.register(251, 149);
        INSTANCE.register(252, 0);
        INSTANCE.register(253, 0);
        INSTANCE.register(254, 0);
        INSTANCE.register(255, 0);
    }
}