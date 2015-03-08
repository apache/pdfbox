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
 * This is specialized CFFEncoding. It's used if the EncodingId of a font is set to 0.
 * 
 * @author Villu Ruusmann
 */
public final class CFFStandardEncoding extends CFFEncoding
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
        INSTANCE.add(33, 2);
        INSTANCE.add(34, 3);
        INSTANCE.add(35, 4);
        INSTANCE.add(36, 5);
        INSTANCE.add(37, 6);
        INSTANCE.add(38, 7);
        INSTANCE.add(39, 8);
        INSTANCE.add(40, 9);
        INSTANCE.add(41, 10);
        INSTANCE.add(42, 11);
        INSTANCE.add(43, 12);
        INSTANCE.add(44, 13);
        INSTANCE.add(45, 14);
        INSTANCE.add(46, 15);
        INSTANCE.add(47, 16);
        INSTANCE.add(48, 17);
        INSTANCE.add(49, 18);
        INSTANCE.add(50, 19);
        INSTANCE.add(51, 20);
        INSTANCE.add(52, 21);
        INSTANCE.add(53, 22);
        INSTANCE.add(54, 23);
        INSTANCE.add(55, 24);
        INSTANCE.add(56, 25);
        INSTANCE.add(57, 26);
        INSTANCE.add(58, 27);
        INSTANCE.add(59, 28);
        INSTANCE.add(60, 29);
        INSTANCE.add(61, 30);
        INSTANCE.add(62, 31);
        INSTANCE.add(63, 32);
        INSTANCE.add(64, 33);
        INSTANCE.add(65, 34);
        INSTANCE.add(66, 35);
        INSTANCE.add(67, 36);
        INSTANCE.add(68, 37);
        INSTANCE.add(69, 38);
        INSTANCE.add(70, 39);
        INSTANCE.add(71, 40);
        INSTANCE.add(72, 41);
        INSTANCE.add(73, 42);
        INSTANCE.add(74, 43);
        INSTANCE.add(75, 44);
        INSTANCE.add(76, 45);
        INSTANCE.add(77, 46);
        INSTANCE.add(78, 47);
        INSTANCE.add(79, 48);
        INSTANCE.add(80, 49);
        INSTANCE.add(81, 50);
        INSTANCE.add(82, 51);
        INSTANCE.add(83, 52);
        INSTANCE.add(84, 53);
        INSTANCE.add(85, 54);
        INSTANCE.add(86, 55);
        INSTANCE.add(87, 56);
        INSTANCE.add(88, 57);
        INSTANCE.add(89, 58);
        INSTANCE.add(90, 59);
        INSTANCE.add(91, 60);
        INSTANCE.add(92, 61);
        INSTANCE.add(93, 62);
        INSTANCE.add(94, 63);
        INSTANCE.add(95, 64);
        INSTANCE.add(96, 65);
        INSTANCE.add(97, 66);
        INSTANCE.add(98, 67);
        INSTANCE.add(99, 68);
        INSTANCE.add(100, 69);
        INSTANCE.add(101, 70);
        INSTANCE.add(102, 71);
        INSTANCE.add(103, 72);
        INSTANCE.add(104, 73);
        INSTANCE.add(105, 74);
        INSTANCE.add(106, 75);
        INSTANCE.add(107, 76);
        INSTANCE.add(108, 77);
        INSTANCE.add(109, 78);
        INSTANCE.add(110, 79);
        INSTANCE.add(111, 80);
        INSTANCE.add(112, 81);
        INSTANCE.add(113, 82);
        INSTANCE.add(114, 83);
        INSTANCE.add(115, 84);
        INSTANCE.add(116, 85);
        INSTANCE.add(117, 86);
        INSTANCE.add(118, 87);
        INSTANCE.add(119, 88);
        INSTANCE.add(120, 89);
        INSTANCE.add(121, 90);
        INSTANCE.add(122, 91);
        INSTANCE.add(123, 92);
        INSTANCE.add(124, 93);
        INSTANCE.add(125, 94);
        INSTANCE.add(126, 95);
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
        INSTANCE.add(161, 96);
        INSTANCE.add(162, 97);
        INSTANCE.add(163, 98);
        INSTANCE.add(164, 99);
        INSTANCE.add(165, 100);
        INSTANCE.add(166, 101);
        INSTANCE.add(167, 102);
        INSTANCE.add(168, 103);
        INSTANCE.add(169, 104);
        INSTANCE.add(170, 105);
        INSTANCE.add(171, 106);
        INSTANCE.add(172, 107);
        INSTANCE.add(173, 108);
        INSTANCE.add(174, 109);
        INSTANCE.add(175, 110);
        INSTANCE.add(176, 0);
        INSTANCE.add(177, 111);
        INSTANCE.add(178, 112);
        INSTANCE.add(179, 113);
        INSTANCE.add(180, 114);
        INSTANCE.add(181, 0);
        INSTANCE.add(182, 115);
        INSTANCE.add(183, 116);
        INSTANCE.add(184, 117);
        INSTANCE.add(185, 118);
        INSTANCE.add(186, 119);
        INSTANCE.add(187, 120);
        INSTANCE.add(188, 121);
        INSTANCE.add(189, 122);
        INSTANCE.add(190, 0);
        INSTANCE.add(191, 123);
        INSTANCE.add(192, 0);
        INSTANCE.add(193, 124);
        INSTANCE.add(194, 125);
        INSTANCE.add(195, 126);
        INSTANCE.add(196, 127);
        INSTANCE.add(197, 128);
        INSTANCE.add(198, 129);
        INSTANCE.add(199, 130);
        INSTANCE.add(200, 131);
        INSTANCE.add(201, 0);
        INSTANCE.add(202, 132);
        INSTANCE.add(203, 133);
        INSTANCE.add(204, 0);
        INSTANCE.add(205, 134);
        INSTANCE.add(206, 135);
        INSTANCE.add(207, 136);
        INSTANCE.add(208, 137);
        INSTANCE.add(209, 0);
        INSTANCE.add(210, 0);
        INSTANCE.add(211, 0);
        INSTANCE.add(212, 0);
        INSTANCE.add(213, 0);
        INSTANCE.add(214, 0);
        INSTANCE.add(215, 0);
        INSTANCE.add(216, 0);
        INSTANCE.add(217, 0);
        INSTANCE.add(218, 0);
        INSTANCE.add(219, 0);
        INSTANCE.add(220, 0);
        INSTANCE.add(221, 0);
        INSTANCE.add(222, 0);
        INSTANCE.add(223, 0);
        INSTANCE.add(224, 0);
        INSTANCE.add(225, 138);
        INSTANCE.add(226, 0);
        INSTANCE.add(227, 139);
        INSTANCE.add(228, 0);
        INSTANCE.add(229, 0);
        INSTANCE.add(230, 0);
        INSTANCE.add(231, 0);
        INSTANCE.add(232, 140);
        INSTANCE.add(233, 141);
        INSTANCE.add(234, 142);
        INSTANCE.add(235, 143);
        INSTANCE.add(236, 0);
        INSTANCE.add(237, 0);
        INSTANCE.add(238, 0);
        INSTANCE.add(239, 0);
        INSTANCE.add(240, 0);
        INSTANCE.add(241, 144);
        INSTANCE.add(242, 0);
        INSTANCE.add(243, 0);
        INSTANCE.add(244, 0);
        INSTANCE.add(245, 145);
        INSTANCE.add(246, 0);
        INSTANCE.add(247, 0);
        INSTANCE.add(248, 146);
        INSTANCE.add(249, 147);
        INSTANCE.add(250, 148);
        INSTANCE.add(251, 149);
        INSTANCE.add(252, 0);
        INSTANCE.add(253, 0);
        INSTANCE.add(254, 0);
        INSTANCE.add(255, 0);
    }
}