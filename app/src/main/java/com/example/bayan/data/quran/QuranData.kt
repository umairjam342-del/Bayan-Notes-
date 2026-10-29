package com.example.bayan.data.quran

import android.content.Context
import android.util.Log
import com.example.bayan.data.model.Ayah
import com.example.bayan.data.model.MapEdge
import com.example.bayan.data.model.MapNode
import com.example.bayan.data.model.MindMap
import com.example.bayan.data.model.Surah
import com.example.bayan.data.model.SurahOrderMode
import org.json.JSONArray
import java.util.concurrent.ConcurrentHashMap

object QuranData {

    val surahs: List<Surah> = listOf(
        Surah(1, "الفَاتِحَة", "Al-Fatihah", "The Opener", 7, "Meccan", 5),
        Surah(2, "البَقَرَة", "Al-Baqarah", "The Cow", 286, "Medinan", 87),
        Surah(3, "آل عِمْرَان", "Ali 'Imran", "Family of Imran", 200, "Medinan", 89),
        Surah(4, "النِّسَاء", "An-Nisa", "The Women", 176, "Medinan", 92),
        Surah(5, "المَائِدَة", "Al-Ma'idah", "The Table Spread", 120, "Medinan", 112),
        Surah(6, "الأَنْعَام", "Al-An'am", "The Cattle", 165, "Meccan", 55),
        Surah(7, "الأَعْرَاف", "Al-A'raf", "The Heights", 206, "Meccan", 39),
        Surah(8, "الأَنْفَال", "Al-Anfal", "The Spoils of War", 75, "Medinan", 88),
        Surah(9, "التَّوْبَة", "At-Tawbah", "The Repentance", 129, "Medinan", 113),
        Surah(10, "يُونُس", "Yunus", "Jonah", 109, "Meccan", 51),
        Surah(11, "هُود", "Hud", "Hud", 123, "Meccan", 52),
        Surah(12, "يُوسُف", "Yusuf", "Joseph", 111, "Meccan", 53),
        Surah(13, "الرَّعْد", "Ar-Ra'd", "The Thunder", 43, "Medinan", 96),
        Surah(14, "إِبْرَاهِيم", "Ibrahim", "Abraham", 52, "Meccan", 72),
        Surah(15, "الحِجْر", "Al-Hijr", "The Rocky Tract", 99, "Meccan", 54),
        Surah(16, "النَّحْل", "An-Nahl", "The Bee", 128, "Meccan", 70),
        Surah(17, "الإِسْرَاء", "Al-Isra", "The Night Journey", 111, "Meccan", 50),
        Surah(18, "الكَهْف", "Al-Kahf", "The Cave", 110, "Meccan", 69),
        Surah(19, "مَرْيَم", "Maryam", "Mary", 98, "Meccan", 44),
        Surah(20, "طه", "Ta-Ha", "Ta-Ha", 135, "Meccan", 45),
        Surah(21, "الأَنْبِيَاء", "Al-Anbiya", "The Prophets", 112, "Meccan", 73),
        Surah(22, "الحَجّ", "Al-Hajj", "The Pilgrimage", 78, "Medinan", 103),
        Surah(23, "المُؤْمِنُون", "Al-Mu'minun", "The Believers", 118, "Meccan", 74),
        Surah(24, "النُّور", "An-Nur", "The Light", 64, "Medinan", 102),
        Surah(25, "الفُرْقَان", "Al-Furqan", "The Criterion", 77, "Meccan", 42),
        Surah(26, "الشُّعَرَاء", "Ash-Shu'ara", "The Poets", 227, "Meccan", 47),
        Surah(27, "النَّمْل", "An-Naml", "The Ant", 93, "Meccan", 48),
        Surah(28, "القَصَص", "Al-Qasas", "The Stories", 88, "Meccan", 49),
        Surah(29, "العَنْكَبُوت", "Al-'Ankabut", "The Spider", 69, "Meccan", 85),
        Surah(30, "الرُّوم", "Ar-Rum", "The Romans", 60, "Meccan", 84),
        Surah(31, "لُقْمَان", "Luqman", "Luqman", 34, "Meccan", 57),
        Surah(32, "السَّجْدَة", "As-Sajdah", "The Prostration", 30, "Meccan", 75),
        Surah(33, "الأَحْزَاب", "Al-Ahzab", "The Combined Forces", 73, "Medinan", 90),
        Surah(34, "سَبَأ", "Saba", "Sheba", 54, "Meccan", 58),
        Surah(35, "فَاطِر", "Fatir", "Originator", 45, "Meccan", 43),
        Surah(36, "يس", "Ya-Sin", "Ya-Sin", 83, "Meccan", 41),
        Surah(37, "الصَّافَّات", "As-Saffat", "Those Who Set The Ranks", 182, "Meccan", 56),
        Surah(38, "ص", "Sad", "The Letter Sad", 88, "Meccan", 38),
        Surah(39, "الزُّمَر", "Az-Zumar", "The Troops", 75, "Meccan", 59),
        Surah(40, "غَافِر", "Ghafir", "The Forgiver", 85, "Meccan", 60),
        Surah(41, "فُصِّلَت", "Fussilat", "Explained In Detail", 54, "Meccan", 61),
        Surah(42, "الشُّورَى", "Ash-Shura", "The Consultation", 53, "Meccan", 62),
        Surah(43, "الزُّخْرُف", "Az-Zukhruf", "The Ornaments of Gold", 89, "Meccan", 63),
        Surah(44, "الدُّخَان", "Ad-Dukhan", "The Smoke", 59, "Meccan", 64),
        Surah(45, "الجَاثِيَة", "Al-Jathiyah", "The Crouching", 37, "Meccan", 65),
        Surah(46, "الأَحْقَاف", "Al-Ahqaf", "The Wind-Curved Sandhills", 35, "Meccan", 66),
        Surah(47, "مُحَمَّد", "Muhammad", "Muhammad", 38, "Medinan", 95),
        Surah(48, "الفَتْح", "Al-Fath", "The Victory", 29, "Medinan", 111),
        Surah(49, "الحُجُرَات", "Al-Hujurat", "The Rooms", 18, "Medinan", 106),
        Surah(50, "ق", "Qaf", "The Letter Qaf", 45, "Meccan", 34),
        Surah(51, "الذَّارِيَات", "Adh-Dhariyat", "The Winnowing Winds", 60, "Meccan", 67),
        Surah(52, "الطُّور", "At-Tur", "The Mount", 49, "Meccan", 76),
        Surah(53, "النَّجْم", "An-Najm", "The Star", 62, "Meccan", 23),
        Surah(54, "القَمَر", "Al-Qamar", "The Moon", 55, "Meccan", 37),
        Surah(55, "الرَّحْمَٰن", "Ar-Rahman", "The Beneficent", 78, "Medinan", 97),
        Surah(56, "الوَاقِعَة", "Al-Waqi'ah", "The Inevitable", 96, "Meccan", 46),
        Surah(57, "الحَدِيد", "Al-Hadid", "The Iron", 29, "Medinan", 94),
        Surah(58, "المُجَادِلَة", "Al-Mujadila", "The Pleading Woman", 22, "Medinan", 105),
        Surah(59, "الحَشْر", "Al-Hashr", "The Exile", 24, "Medinan", 101),
        Surah(60, "المُمْتَحَنَة", "Al-Mumtahanah", "She That Is To Be Examined", 13, "Medinan", 91),
        Surah(61, "الصَّفّ", "As-Saff", "The Ranks", 14, "Medinan", 109),
        Surah(62, "الجُمُعَة", "Al-Jumu'ah", "The Congregation, Friday", 11, "Medinan", 110),
        Surah(63, "المُنَافِقُون", "Al-Munafiqun", "The Hypocrites", 11, "Medinan", 104),
        Surah(64, "التَّغَابُن", "At-Taghabun", "The Mutual Disillusion", 18, "Medinan", 108),
        Surah(65, "الطَّلَاق", "At-Talaq", "The Divorce", 12, "Medinan", 99),
        Surah(66, "التَّحْرِيم", "At-Tahrim", "The Prohibition", 12, "Medinan", 107),
        Surah(67, "المُلْك", "Al-Mulk", "The Sovereignty", 30, "Meccan", 77),
        Surah(68, "القَلَم", "Al-Qalam", "The Pen", 52, "Meccan", 2),
        Surah(69, "الحَاقَّة", "Al-Haqqah", "The Reality", 52, "Meccan", 78),
        Surah(70, "المَعَارِج", "Al-Ma'arij", "The Ascending Stairways", 44, "Meccan", 79),
        Surah(71, "نُوح", "Nuh", "Noah", 28, "Meccan", 71),
        Surah(72, "الجِنّ", "Al-Jinn", "The Jinn", 28, "Meccan", 40),
        Surah(73, "المُزَّمِّل", "Al-Muzzammil", "The Enshrouded One", 20, "Meccan", 3),
        Surah(74, "المُدَّثِّر", "Al-Muddaththir", "The Cloaked One", 56, "Meccan", 4),
        Surah(75, "القِيَامَة", "Al-Qiyamah", "The Resurrection", 40, "Meccan", 31),
        Surah(76, "الإِنْسَان", "Al-Insan", "Man", 31, "Medinan", 98),
        Surah(77, "المُرْسَلَات", "Al-Mursalat", "The Emissaries", 50, "Meccan", 33),
        Surah(78, "النَّبَأ", "An-Naba", "The Tidings", 40, "Meccan", 80),
        Surah(79, "النَّازِعَات", "An-Nazi'at", "Those Who Drag Forth", 46, "Meccan", 81),
        Surah(80, "عَبَسَ", "'Abasa", "He Frowned", 42, "Meccan", 24),
        Surah(81, "التَّكْوِير", "At-Takwir", "The Overthrowing", 29, "Meccan", 7),
        Surah(82, "الانْفِطَار", "Al-Infitar", "The Cleaving", 19, "Meccan", 82),
        Surah(83, "المُطَفِّفِين", "Al-Mutaffifin", "Defrauding", 36, "Meccan", 86),
        Surah(84, "الانْشِقَاق", "Al-Inshiqaq", "The Splitting Open", 25, "Meccan", 83),
        Surah(85, "البُرُوج", "Al-Buruj", "The Mansions of the Stars", 22, "Meccan", 27),
        Surah(86, "الطَّارِق", "At-Tariq", "The Morning Star", 17, "Meccan", 36),
        Surah(87, "الأَعْلَى", "Al-A'la", "The Most High", 19, "Meccan", 8),
        Surah(88, "الغَاشِيَة", "Al-Ghashiyah", "The Overwhelming", 26, "Meccan", 68),
        Surah(89, "الفَجْر", "Al-Fajr", "The Dawn", 30, "Meccan", 10),
        Surah(90, "البَلَد", "Al-Balad", "The City", 20, "Meccan", 35),
        Surah(91, "الشَّمْس", "Ash-Shams", "The Sun", 15, "Meccan", 26),
        Surah(92, "اللَّيْل", "Al-Layl", "The Night", 21, "Meccan", 9),
        Surah(93, "الضُّحَى", "Ad-Duha", "The Morning Hours", 11, "Meccan", 11),
        Surah(94, "الشَّرْح", "Ash-Sharh", "The Relief", 8, "Meccan", 12),
        Surah(95, "التِّين", "At-Tin", "The Fig", 8, "Meccan", 28),
        Surah(96, "العَلَق", "Al-'Alaq", "The Clot", 19, "Meccan", 1),
        Surah(97, "القَدْر", "Al-Qadr", "The Power, Fate", 5, "Meccan", 25),
        Surah(98, "البَيِّنَة", "Al-Bayyinah", "The Clear Proof", 8, "Medinan", 100),
        Surah(99, "الزَّلْزَلَة", "Az-Zalzalah", "The Earthquake", 8, "Medinan", 93),
        Surah(100, "العَادِيَات", "Al-'Adiyat", "The Courser", 11, "Meccan", 14),
        Surah(101, "القَارِعَة", "Al-Qari'ah", "The Calamity", 11, "Meccan", 30),
        Surah(102, "التَّكَاثُر", "At-Takathur", "The Rivalry in World Increase", 8, "Meccan", 16),
        Surah(103, "العَصْر", "Al-'Asr", "The Declining Day", 3, "Meccan", 13),
        Surah(104, "الهُمَزَة", "Al-Humazah", "The Traducer", 9, "Meccan", 32),
        Surah(105, "الفِيل", "Al-Fil", "The Elephant", 5, "Meccan", 19),
        Surah(106, "قُرَيْش", "Quraysh", "Quraysh", 4, "Meccan", 29),
        Surah(107, "المَاعُون", "Al-Ma'un", "Small Kindnesses", 7, "Meccan", 17),
        Surah(108, "الكَوْثَر", "Al-Kawthar", "The Abundance", 3, "Meccan", 15),
        Surah(109, "الكَافِرُون", "Al-Kafirun", "The Disbelievers", 6, "Meccan", 18),
        Surah(110, "النَّصْر", "An-Nasr", "The Divine Support", 3, "Medinan", 114),
        Surah(111, "المَسَد", "Al-Masad", "The Palm Fibre", 5, "Meccan", 6),
        Surah(112, "الإِخْلَاص", "Al-Ikhlas", "The Sincerity", 4, "Meccan", 22),
        Surah(113, "الفَلَق", "Al-Falaq", "The Daybreak", 5, "Meccan", 20),
        Surah(114, "النَّاس", "An-Nas", "Mankind", 6, "Meccan", 21)
    )

    val surahsByRevelation: List<Surah> by lazy {
        surahs.sortedBy { it.revelationOrder }
    }

    fun getSurahs(orderMode: SurahOrderMode = SurahOrderMode.MUSHAF): List<Surah> {
        return when (orderMode) {
            SurahOrderMode.MUSHAF -> surahs
            SurahOrderMode.REVELATION -> surahsByRevelation
        }
    }

    // Pre-loaded detailed verse datasets with authentic Arabic and Tafsir notes
    val sampleAyahs: Map<Int, List<Ayah>> = mapOf(
        1 to listOf(
            Ayah(
                1, 1,
                "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                "شروع الله کا نام لے کر جو بڑا مہربان نہایت رحم والا ہے",
                "Bismillahi r-rahmani r-rahim",
                "Tafsir Ibn Kathir: The Basmalah is a declaration of seeking aid through Allah's names before embarking upon any noble endeavor. Ar-Rahman denotes the vast, all-encompassing mercy for all creation; Ar-Rahim denotes the specific, eternal mercy bestowed upon the believers."
            ),
            Ayah(
                1, 2,
                "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ",
                "سب طرح کی تعریف خدا ہی کو (سزاوار) ہے جو تمام مخلوقات کا پروردگار ہے",
                "Al-hamdu lillahi rabbi l-'alamin",
                "Tafsir As-Sa'di: Hamd combines gratitude and love with glorification. 'Rabb' signifies the Creator, Sovereign, Sustainer, and Nurturer of all existence ('Alamin)."
            ),
            Ayah(
                1, 3,
                "الرَّحْمَٰنِ الرَّحِيمِ",
                "بڑا مہربان نہایت رحم والا",
                "Ar-rahmani r-rahim",
                "Tafsir Al-Qurtubi: Re-affirming divine mercy directly after lordship ('Rabb') assures the servant that divine sovereignty is exercised through grace rather than harsh tyranny."
            ),
            Ayah(
                1, 4,
                "مَالِكِ يَوْمِ الدِّينِ",
                "انصاف کے دن کا حاکم",
                "Maliki yawmi d-din",
                "Tafsir Al-Jalalayn: 'Din' here denotes reckoning and compensation. While Allah is Sovereign of all days, His absolute sovereignty will be unmistakably manifest to all beings on that Day."
            ),
            Ayah(
                1, 5,
                "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ",
                "(اے پروردگار) ہم تیری ہی عبادت کرتے ہیں اور تجھ ہی سے مدد مانگتے ہیں",
                "Iyyaka na'budu wa-iyyaka nasta'in",
                "Theological Gem: The grammatical fronting of the object ('Iyyaka') signifies exclusivity (Hasr): 'You alone do we worship, and from You alone do we seek aid.' Worship is the goal; seeking help is the prerequisite."
            ),
            Ayah(
                1, 6,
                "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ",
                "ہم کو سیدھے رستے چلا",
                "Ihdina s-sirata l-mustaqim",
                "Tafsir Ibn Ashur: Guidance (Hidayah) here encompasses both showing the path (Irshad) and granting the enablement (Tawfiq) to remain steadfast upon it."
            ),
            Ayah(
                1, 7,
                "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ",
                "ان لوگوں کے رستے جن پر تو اپنا فضل وکرم کرتا رہا نہ ان کے جن پر غصے ہوتا رہا اور نہ گمراہوں کے",
                "Sirata l-ladhina an'amta 'alayhim ghayri l-maghdubi 'alayhim wa-la d-dallin",
                "Tafsir Ibn Kathir: Those favored are the Prophets, the Truthful (Siddiqin), the Martyrs (Shuhada), and the Righteous (Salihin) as elaborated in Surah An-Nisa (4:69)."
            )
        ),
        2 to listOf(
            Ayah(
                2, 1,
                "الم",
                "الٓمٓ",
                "Alif-Lam-Meem",
                "I'jaz al-Quran: The Huruf Muqatta'at (disjointed letters) challenge the master Arabic orators of Arabia: the Quran is composed of the very letters of your alphabet, yet humanity is powerless to produce a single chapter like it."
            ),
            Ayah(
                2, 2,
                "ذَٰلِكَ الْكِتَابُ لَا رَيْبَ ۛ فِيهِ ۛ هُدًى لِّلْمُتَّقِينَ",
                "یہ وہ کتاب ہے جس میں کوئی بھی شک نہیں، پرہیزگاروں کے لیے ہدایت ہے",
                "Dhalika l-kitabu la rayba fih, hudan lil-muttaqin",
                "Tafsir As-Sa'di: Absolute certainty ('La rayba fih') combined with practical guidance. 'Taqwa' acts as the fertile soil allowing revelation to bear transformative fruit."
            ),
            Ayah(
                2, 3,
                "الَّذِينَ يُؤْمِنُونَ بِالْغَيْبِ وَيُقِيمُونَ الصَّلَاةَ وَمِمَّا رَزَقْنَاهُمْ يُنفِقُونَ",
                "جو غیب پر ایمان لاتے اور نماز قائم کرتے اور جو کچھ ہم نے ان کو دیا ہے اس میں سے خرچ کرتے ہیں",
                "Al-ladhina yu'minuna bil-ghaybi wa-yuqimuna s-salata wa-mimma razaqnahum yunfiqun",
                "Theological Gem: Faith in the Unseen (Ghayb) elevates human perception beyond animalistic materialism into transcendental reality. Establishing prayer fosters vertical spiritual connection; spending fosters horizontal societal justice."
            ),
            Ayah(
                2, 255,
                "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ",
                "خدا (وہ معبود برحق ہے کہ) اس کے سوا کوئی معبود نہیں زندہ ہمیشہ رہنے والا، نہ اس کو اونگھ آتی ہے نہ نیند، جو کچھ آسمانوں میں اور جو کچھ زمین میں ہے سب اسی کا ہے، کون ہے جو اس کی اجازت کے بغیر اس کے سامنے کسی کی سفارش کر سکے، جو کچھ لوگوں کے روبرو ہو رہا ہے اور جو کچھ ان کے پیچھے ہو چکا ہے اسے سب معلوم ہے، اور وہ اس کی معلومات میں سے کسی چیز پر احاطہ نہیں کر سکتے مگر جتنا وہ چاہے، اس کی کرسی آسمانوں اور زمین پر چھائی ہوئی ہے اور ان کی حفاظت اس کو نہیں تھکاتی، اور وہ عالی رتبہ اور جلیل القدر ہے",
                "Allahu la ilaha illa huwa l-hayyu l-qayyum...",
                "Ayat Al-Kursi (The Greatest Verse): A masterpiece of monotheistic theology containing ten distinct ontological declarations of divine perfection, self-subsistence, universal ownership, omniscience, and sublime majesty."
            )
        ),
        18 to listOf(
            Ayah(
                18, 1,
                "الْحَمْدُ لِلَّهِ الَّذِي أَنزَلَ عَلَىٰ عَبْدِهِ الْكِتَابَ وَلَمْ يَجْعَل لَّهُ عِوَجًا",
                "سب تعریفیں اللہ ہی کے لیے ہیں جس نے اپنے بندے پر یہ کتاب اتاری اور اس میں کوئی کجی نہ رکھی",
                "Al-hamdu lillahi l-ladhi anzala 'ala 'abdihi l-kitaba wa-lam yaj'al lahu 'iwaja",
                "Tafsir Al-Baghawi: Praise for the descent of unadulterated divine guidance. 'Iwaj' represents conceptual ambiguity, moral crookedness, or contradiction—all of which the Quran is miraculously free from."
            ),
            Ayah(
                18, 10,
                "إِذْ أَوَى الْفِتْيَةُ إِلَى الْكَهْفِ فَقَالُوا رَبَّنَا آتِنَا مِن لَّدُنكَ رَحْمَةً وَهَيِّئْ لَنَا مِنْ أَمْرِنَا رَشَدًا",
                "جب وہ چند نوجوان غار میں پناہ گزین ہوئے تو کہنے لگے: اے ہمارے رب! ہمیں اپنے پاس سے رحمت عطا فرما اور ہمارے لیے ہمارے کام میں رہنمائی کا سامان پیدا کر",
                "Idh awa l-fityatu ila l-kahfi faqalu rabbana atina min ladunka rahmatan wa-hayyi' lana min amrina rashada",
                "Tadabbur Note: A profound supplication for youth preserving faith against institutional corruption. They prioritized inward mercy and pragmatic rectitude ('Rashad') over worldly prestige."
            )
        ),
        112 to listOf(
            Ayah(
                112, 1,
                "قُلْ هُوَ اللَّهُ أَحَدٌ",
                "کہو کہ وہ (ذات پاک جس کا نام) الله (ہے) ایک ہے",
                "Qul huwa Allahu ahad",
                "Tafsir: Ahad indicates indivisible, absolute oneness that cannot be multiplied, partitioned, or compared to any entity."
            ),
            Ayah(
                112, 2,
                "اللَّهُ الصَّمَدُ",
                "معبود برحق جو بےنیاز ہے",
                "Allahu s-samad",
                "Linguistic Miracle: As-Samad denotes the Master who is independently complete in all attributes, upon Whom every creation depends while He depends on none."
            ),
            Ayah(
                112, 3,
                "لَمْ يَلِدْ وَلَمْ يُولَدْ",
                "نہ اس کی کوئی اولاد ہے اور نہ وہ کسی کی اولاد ہے",
                "Lam yalid wa-lam yulad",
                "Theological Gem: Refutation of anthropomorphism, mythological genealogies, and filial attribution to God."
            ),
            Ayah(
                112, 4,
                "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ",
                "اور کوئی اس کا ہمسر نہیں",
                "Wa-lam yakun lahu kufuwan ahad",
                "Transcendent Principle: Total negating of any equal, parallel, peer, or likeness in essence, sovereignty, or attributes."
            )
        )
    )

    private val surahCache = ConcurrentHashMap<Int, List<Ayah>>()

    /**
     * Retrieves the complete authentic Uthmani text, English translation, and classical tafsir
     * for any Surah (1 to 114) from the bundled asset store with in-memory caching.
     */
    fun getAyahsForSurah(context: Context, surahNumber: Int): List<Ayah> {
        surahCache[surahNumber]?.let { return it }

        val loaded = try {
            val jsonString = context.assets.open("quran/surah_$surahNumber.json")
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<Ayah>()
            val samples = sampleAyahs[surahNumber]?.associateBy { it.ayahNumber } ?: emptyMap()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val aNum = obj.getInt("ayahNumber")
                val sNum = obj.getInt("surahNumber")
                val arabic = obj.getString("arabicText")
                val translation = obj.getString("translation")
                val tafsirFromJson = obj.optString("tafsirExcerpt", "")

                val sampleMatch = samples[aNum]
                val transliteration = sampleMatch?.transliteration ?: ""
                val tafsir = if (sampleMatch != null && sampleMatch.tafsirExcerpt.isNotBlank()) {
                    sampleMatch.tafsirExcerpt
                } else {
                    tafsirFromJson
                }

                list.add(
                    Ayah(
                        surahNumber = sNum,
                        ayahNumber = aNum,
                        arabicText = arabic,
                        translation = translation,
                        transliteration = transliteration,
                        tafsirExcerpt = tafsir
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.e("QuranData", "Error reading surah_$surahNumber.json from assets", e)
            sampleAyahs[surahNumber] ?: emptyList()
        }

        if (loaded.isNotEmpty()) {
            surahCache[surahNumber] = loaded
        }
        return loaded
    }

    /**
     * Retrieve a specific Ayah by Surah and Ayah number from memory cache or asset store.
     */
    fun getAyah(context: Context, surahNumber: Int, ayahNumber: Int): Ayah? {
        val ayahs = getAyahsForSurah(context, surahNumber)
        return ayahs.firstOrNull { it.ayahNumber == ayahNumber }
    }

    // Theological Lexicon & Root Word Database
    data class RootEntry(
        val rootArabic: String,
        val transliteration: String,
        val primaryMeaning: String,
        val occurrencesCount: Int,
        val theologicalSignificance: String,
        val derivativeForms: List<String>,
        val sampleVerses: List<String>
    )

    val theologicalRoots: List<RootEntry> = listOf(
        RootEntry(
            "ب ي ن",
            "B-Y-N",
            "Clarity, manifestation, articulate exposition, separation of truth from falsehood",
            523,
            "The root of 'Bayan'—the divine bestowal of articulate expression and clear proof. Represents the Quran's nature as 'Mubin' (manifest and elucidating).",
            listOf("بَيَان (Bayan - Clear Speech)", "مُبِين (Mubin - Evident)", "تِبْيَان (Tibyan - Comprehensive Explanation)", "بَيِّنَة (Bayyinah - Clear Proof)"),
            listOf("عَلَّمَهُ الْبَيَانَ (55:4)", "وَتَفْصِيلَ الْكِتَابِ لَا رَيْبَ فِيهِ (10:37)", "قَد تَّبَيَّنَ الرُّشْدُ مِنَ الْغَيِّ (2:256)")
        ),
        RootEntry(
            "ر ح م",
            "R-H-M",
            "Womb, compassionate tenderness, mercy, protective nurture, beneficence",
            339,
            "The foundational relational attribute of Allah toward creation. Evokes maternal nurture ('Rahim') elevated to infinite, transcendent divine grace.",
            listOf("الرَّحْمَٰن (Ar-Rahman - Cosmic Grace)", "الرَّحِيم (Ar-Rahim - Covenantal Mercy)", "رَحْمَة (Rahmah - Divine Compassion)"),
            listOf("وَرَحْمَتِي وَسِعَتْ كُلَّ شَيْءٍ (7:156)", "إِنَّ رَحْمَتَ اللَّهِ قَرِيبٌ مِّنَ الْمُحْسِنِينَ (7:56)")
        ),
        RootEntry(
            "ح م د",
            "H-M-D",
            "Praise with reverence, gratitude coupled with admiration, exaltation",
            68,
            "Distinguished from Madh (flattery) and Shukr (gratitude for favors): Hamd is offered unconditionally for Allah's intrinsic majesty and bounties.",
            listOf("الحَمْد (Al-Hamd - Universal Praise)", "مَحْمُود (Mahmud - Praiseworthy)", "أَحْمَد (Ahmad - Most Praising)"),
            listOf("الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ (1:2)", "وَلَهُ الْحَمْدُ فِي الْآخِرَةِ (34:1)")
        ),
        RootEntry(
            "ع ل م",
            "'-L-M",
            "Knowledge, mark/sign, cognitive awareness, wisdom of reality",
            854,
            "Omniscience of the Divine and the elevated status of sound theological knowledge ('Ilm) over conjectural skepticism ('Dhann').",
            listOf("العَلِيم (Al-'Alim - All-Knowing)", "عَالَم (Alam - Realm/World)", "مَعْلُوم (Ma'lum - Known)"),
            listOf("وَعَلَّمَ آدَمَ الْأَسْمَاءَ كُلَّهَا (2:31)", "وَقُل رَّبِّ زِدْنِي عِلْمًا (20:114)")
        ),
        RootEntry(
            "ح ك م",
            "H-K-M",
            "Wisdom, governance, judicial resolution, structural restraint from corruption",
            210,
            "Combines just authority (Hukm) with consummate wisdom (Hikmah). Revelation acts as the final judge between competing truth claims.",
            listOf("الحَكِيم (Al-Hakim - The Wise)", "الحُكْم (Al-Hukm - Authority/Judgment)", "حِكْمَة (Hikmah - Profound Wisdom)"),
            listOf("إِنِ الْحُكْمُ إِلَّا لِلَّهِ (12:40)", "يُؤْتِي الْحِكْمَةَ مَن يَشَاءُ (2:269)")
        ),
        RootEntry(
            "ق د ر",
            "Q-D-R",
            "Measurement, divine decree, capability, intrinsic worth, divine power",
            132,
            "Connects the concept of divine decree (Qadar) with absolute omnipotence (Qudrah) and majestic dignity (Qadr).",
            listOf("القَدِير (Al-Qadir - The Capable)", "قَدَر (Qadar - Ordained Measure)", "قَدْر (Qadr - Inestimable Worth)"),
            listOf("إِنَّا كُلَّ شَيْءٍ خَلَقْنَاهُ بِقَدَرٍ (54:49)", "إِنَّا أَنزَلْنَاهُ فِي لَيْلَةِ الْقَدْرِ (97:1)")
        )
    )

    // Pre-built Mind Map Templates
    fun getPrebuiltMindMaps(): List<MindMap> {
        val fatihahNodes = listOf(
            MapNode("f1", "Surah Al-Fatihah", "الفاتحة", "Core Theme", "#0D5341", 300f, 60f, "The Mother of the Book (Umm al-Kitab)"),
            MapNode("f2", "Divine Names & Attributes", "الأسماء والصفات", "Theological Gem", "#907000", 120f, 220f, "Verses 1-3: Rububiyyah, Rahmaniyyah, Rahimiyyah"),
            MapNode("f3", "Eschatology & Sovereignty", "يوم الدين", "Core Theme", "#245F6D", 300f, 240f, "Verse 4: Absolute accountability on the Day of Recompense"),
            MapNode("f4", "The Central Covenant", "إياك نعبد وإياك نستعين", "Theological Gem", "#0D5341", 480f, 220f, "Verse 5: Sincere devotion and reliance (Tawakkul)"),
            MapNode("f5", "The Straight Path", "الصراط المستقيم", "Core Theme", "#1B5E20", 300f, 400f, "Verses 6-7: The comprehensive path of divine grace")
        )
        val fatihahEdges = listOf(
            MapEdge("e1", "f1", "f2", "Reveals"),
            MapEdge("e2", "f1", "f3", "Affirms"),
            MapEdge("e3", "f1", "f4", "Synthesizes"),
            MapEdge("e4", "f4", "f5", "Leads to prayer for")
        )

        val kahfNodes = listOf(
            MapNode("k1", "Surah Al-Kahf", "الكهف", "Core Theme", "#0D5341", 300f, 60f, "The 4 Great Tribulations & Their Antidotes"),
            MapNode("k2", "Trial of Faith", "فتنة الدين", "Prophetic Story", "#1E88E5", 100f, 200f, "The Companions of the Cave -> Antidote: Sincere Sahaba & Tawakkul"),
            MapNode("k3", "Trial of Wealth", "فتنة المال", "Prophetic Story", "#F57C00", 250f, 320f, "The Two Garden Owners -> Antidote: Remembering the Hereafter"),
            MapNode("k4", "Trial of Knowledge", "فتنة العلم", "Prophetic Story", "#7B1FA2", 350f, 200f, "Musa & Al-Khidr -> Antidote: Humility and Patience (Sabr)"),
            MapNode("k5", "Trial of Power", "فتنة السلطة", "Prophetic Story", "#C2185B", 500f, 320f, "Dhul-Qarnayn -> Antidote: Justice and Taqwa")
        )
        val kahfEdges = listOf(
            MapEdge("ke1", "k1", "k2", "Section 1"),
            MapEdge("ke2", "k1", "k3", "Section 2"),
            MapEdge("ke3", "k1", "k4", "Section 3"),
            MapEdge("ke4", "k1", "k5", "Section 4")
        )

        return listOf(
            MindMap(
                id = "template_fatihah",
                title = "Structure of Surah Al-Fatihah",
                description = "Architectural thematic analysis of the Seven Oft-Repeated Verses",
                surahNumber = 1L,
                nodesJson = serializeNodes(fatihahNodes),
                edgesJson = serializeEdges(fatihahEdges)
            ),
            MindMap(
                id = "template_kahf",
                title = "The Four Trials of Surah Al-Kahf",
                description = "Thematic mapping of the four stories and their spiritual antidotes",
                surahNumber = 18L,
                nodesJson = serializeNodes(kahfNodes),
                edgesJson = serializeEdges(kahfEdges)
            )
        )
    }

    fun serializeNodes(nodes: List<MapNode>): String {
        val sb = StringBuilder("[")
        nodes.forEachIndexed { index, node ->
            sb.append("{")
            sb.append("\"id\":\"${node.id}\",")
            sb.append("\"label\":\"${escape(node.label)}\",")
            sb.append("\"arabicText\":\"${escape(node.arabicText)}\",")
            sb.append("\"category\":\"${escape(node.category)}\",")
            sb.append("\"colorHex\":\"${node.colorHex}\",")
            sb.append("\"posX\":${node.posX},")
            sb.append("\"posY\":${node.posY},")
            sb.append("\"notes\":\"${escape(node.notes)}\"")
            sb.append("}")
            if (index < nodes.size - 1) sb.append(",")
        }
        sb.append("]")
        return sb.toString()
    }

    fun serializeEdges(edges: List<MapEdge>): String {
        val sb = StringBuilder("[")
        edges.forEachIndexed { index, edge ->
            sb.append("{")
            sb.append("\"id\":\"${edge.id}\",")
            sb.append("\"fromNodeId\":\"${edge.fromNodeId}\",")
            sb.append("\"toNodeId\":\"${edge.toNodeId}\",")
            sb.append("\"label\":\"${escape(edge.label)}\"")
            sb.append("}")
            if (index < edges.size - 1) sb.append(",")
        }
        sb.append("]")
        return sb.toString()
    }

    private fun escape(text: String): String {
        return text.replace("\"", "\\\"").replace("\n", "\\n")
    }

    fun deserializeNodes(json: String): List<MapNode> {
        val list = mutableListOf<MapNode>()
        if (json.isBlank() || json == "[]") return list
        try {
            val trimmed = json.trim().removeSurrounding("[", "]")
            val items = trimmed.split("},{")
            for (item in items) {
                val clean = item.removePrefix("{").removeSuffix("}")
                val map = parseKeyValuePairs(clean)
                list.add(
                    MapNode(
                        id = map["id"] ?: "node_${System.currentTimeMillis()}",
                        label = map["label"] ?: "Concept",
                        arabicText = map["arabicText"] ?: "",
                        category = map["category"] ?: "Core Theme",
                        colorHex = map["colorHex"] ?: "#0D5341",
                        posX = map["posX"]?.toFloatOrNull() ?: 200f,
                        posY = map["posY"]?.toFloatOrNull() ?: 200f,
                        notes = map["notes"] ?: ""
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun deserializeEdges(json: String): List<MapEdge> {
        val list = mutableListOf<MapEdge>()
        if (json.isBlank() || json == "[]") return list
        try {
            val trimmed = json.trim().removeSurrounding("[", "]")
            val items = trimmed.split("},{")
            for (item in items) {
                val clean = item.removePrefix("{").removeSuffix("}")
                val map = parseKeyValuePairs(clean)
                list.add(
                    MapEdge(
                        id = map["id"] ?: "edge_${System.currentTimeMillis()}",
                        fromNodeId = map["fromNodeId"] ?: "",
                        toNodeId = map["toNodeId"] ?: "",
                        label = map["label"] ?: "Relates to"
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun parseKeyValuePairs(str: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val parts = str.split(",\"")
        for (part in parts) {
            val cleanPart = part.removePrefix("\"")
            val colonIdx = cleanPart.indexOf("\":")
            if (colonIdx != -1) {
                val key = cleanPart.substring(0, colonIdx)
                val value = cleanPart.substring(colonIdx + 2).removeSurrounding("\"")
                result[key] = value.replace("\\\"", "\"").replace("\\n", "\n")
            }
        }
        return result
    }
}
