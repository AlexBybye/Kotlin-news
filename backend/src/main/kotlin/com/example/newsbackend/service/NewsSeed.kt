package com.example.newsbackend.service

import com.example.newsbackend.model.NewsArticleDto
import java.security.MessageDigest

/**
 * 内置中文校园新闻种子数据。
 * 当数据库为空时灌入，保证后端离线也有可展示的新闻（课程演示友好）。
 * id 由标题哈希生成，与 NewsService.stableId 同算法，保证幂等、可重复 upsert。
 */
object NewsSeed {

    /** 一条种子文章：除列表字段外，附带详情正文段落。 */
    data class SeedArticle(
        val title: String,
        val summary: String,
        val source: String,
        val author: String?,
        val category: String,
        val publishTime: String,
        val coverImageUrl: String?,
        val contentUrl: String?,
        val isTop: Boolean,
        val content: List<String>
    )

    private fun stableId(seed: String): String {
        val digest = MessageDigest.getInstance("MD5").digest(seed.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }.take(16)
    }

    fun toArticleDto(seed: SeedArticle): NewsArticleDto = NewsArticleDto(
        id = stableId(seed.title),
        title = seed.title,
        summary = seed.summary,
        coverImageUrl = seed.coverImageUrl,
        author = seed.author,
        source = seed.source,
        category = seed.category,
        publishTime = seed.publishTime,
        contentUrl = seed.contentUrl,
        isTop = seed.isTop
    )

    /** 返回 (DTO 列表, 详情正文映射)，供 NewsRepository.upsertAll 使用。 */
    fun asUpsertPayload(): Pair<List<NewsArticleDto>, Map<String, List<String>>> {
        val dtos = articles.map { toArticleDto(it) }
        val contentMap = articles.associate { stableId(it.title) to it.content }
        return dtos to contentMap
    }

    private val img = "https://picsum.photos/seed"

    val articles: List<SeedArticle> = listOf(
        // ---------------- 校园 campus ----------------
        SeedArticle(
            title = "我校2026届毕业生春季校园双选会成功举办，进场企业逾300家",
            summary = "近日，2026届毕业生春季校园双选会在体育馆举行，300余家用人单位提供岗位上万个，覆盖信息技术、智能制造、教育等多个领域。",
            source = "校园新闻网", author = "学生记者团", category = "campus",
            publishTime = "2026-06-12", coverImageUrl = "$img/campus1/800/450",
            contentUrl = "https://example.edu.cn/news/spring-job-fair", isTop = true,
            content = listOf(
                "6月12日上午，由校就业指导中心主办的2026届毕业生春季校园双选会在学校体育馆顺利举行。本次双选会共吸引300余家用人单位参会，提供就业岗位逾万个，涵盖信息技术、智能制造、金融、教育、医疗等多个行业领域。",
                "上午八点不到，体育馆门口便排起长队。许多同学提前准备了精心制作的简历，在各招聘展位间穿梭咨询。计算机学院的张同学表示，现场气氛热烈，企业HR也十分耐心，自己已经投出了五份简历并获得两个面试机会。",
                "就业指导中心负责人介绍，为提升供需匹配效率，本次双选会首次引入线上预约与岗位智能推荐系统，学生可提前查看企业需求并预约面试时段。下一步学校还将举办多场行业专场招聘活动，全力护航毕业生高质量就业。"
            )
        ),
        SeedArticle(
            title = "图书馆24小时自习区正式启用，新增智能座位预约系统",
            summary = "为满足学生备考与学习需求，图书馆一楼24小时自习区即日起开放，配套上线的座位预约小程序可实时查看空位。",
            source = "校园新闻网", author = "图书馆办公室", category = "campus",
            publishTime = "2026-06-10", coverImageUrl = "$img/campus2/800/450",
            contentUrl = "https://example.edu.cn/news/library-24h", isTop = false,
            content = listOf(
                "为更好地服务师生学习需求，图书馆历经两个月改造的一楼24小时自习区于本周正式启用。该区域共设置座位240个，配备独立台灯、电源插座与静音隔断，全天候开放。",
                "与自习区同步上线的还有智能座位预约系统。学生通过校园小程序即可实时查看各楼层空位情况、预约心仪座位，并支持扫码签到与超时自动释放，有效解决了以往“占座”难题。",
                "据悉，图书馆后续还将对二至四楼阅览区进行分批升级，并扩充电子资源数据库，进一步提升服务品质。"
            )
        ),
        SeedArticle(
            title = "第十六届校园文化艺术节开幕，百余项活动陆续登场",
            summary = "以“青春向党、强国有我”为主题的第十六届校园文化艺术节正式拉开帷幕，将持续一个月，涵盖文艺汇演、社团展示、创意市集等。",
            source = "校团委", author = null, category = "campus",
            publishTime = "2026-06-08", coverImageUrl = "$img/campus3/800/450",
            contentUrl = "https://example.edu.cn/news/art-festival", isTop = false,
            content = listOf(
                "6月8日晚，第十六届校园文化艺术节开幕式在大学生活动中心隆重举行。本届艺术节以“青春向党、强国有我”为主题，将在一个月内陆续推出文艺汇演、社团巡礼、创意市集、辩论赛等百余项活动。",
                "开幕式上，各院系学生带来了歌舞、器乐、戏剧等精彩节目，现场座无虚席、掌声不断。校团委负责人表示，希望通过丰富多彩的活动为同学们搭建展示自我、交流成长的舞台。"
            )
        ),
        SeedArticle(
            title = "学校与多家头部企业共建产业学院，深化产教融合",
            summary = "学校近日与多家行业龙头企业签署合作协议，共建人工智能、集成电路等现代产业学院，推动人才培养与产业需求精准对接。",
            source = "校园新闻网", author = "宣传部", category = "campus",
            publishTime = "2026-06-05", coverImageUrl = "$img/campus4/800/450",
            contentUrl = "https://example.edu.cn/news/industry-college", isTop = false,
            content = listOf(
                "为深化产教融合、协同育人，学校近日与多家行业龙头企业举行合作签约仪式，共建人工智能产业学院、集成电路产业学院等多个现代产业学院。",
                "根据协议，校企双方将在课程共建、师资互聘、实习实训、联合科研等方面展开深度合作，把产业真实项目引入课堂，让学生在校期间就能接触前沿技术与工程实践。"
            )
        ),

        // ---------------- 科技 technology ----------------
        SeedArticle(
            title = "我校科研团队在新型固态电池领域取得重要突破",
            summary = "材料学院科研团队研发出一种新型固态电解质，使电池能量密度与安全性显著提升，相关成果发表于国际权威期刊。",
            source = "科技日报", author = "李研究员", category = "technology",
            publishTime = "2026-06-13", coverImageUrl = "$img/tech1/800/450",
            contentUrl = "https://example.edu.cn/news/solid-battery", isTop = true,
            content = listOf(
                "近日，我校材料科学与工程学院科研团队在新型固态电池领域取得重要进展。团队成功研发出一种高离子电导率的固态电解质材料，在大幅提升电池能量密度的同时显著改善了安全性能。",
                "据团队负责人介绍，传统液态锂电池存在易燃、易漏液等安全隐患，而固态电池被视为下一代储能技术的重要方向。此次研发的新材料在常温下即可实现优异的离子传输性能，为固态电池产业化奠定了基础。",
                "相关研究成果已发表于国际权威期刊，并申请多项发明专利。下一步团队将与企业合作推进中试与量产验证。"
            )
        ),
        SeedArticle(
            title = "人工智能大模型如何重塑高校教学？多所高校展开探索",
            summary = "随着生成式人工智能技术快速发展，越来越多高校将大模型引入课堂，用于个性化辅导、智能答疑与编程教学。",
            source = "中国教育报", author = null, category = "technology",
            publishTime = "2026-06-11", coverImageUrl = "$img/tech2/800/450",
            contentUrl = "https://example.edu.cn/news/ai-teaching", isTop = false,
            content = listOf(
                "近年来，以大语言模型为代表的生成式人工智能技术迅猛发展，正在深刻改变高等教育的教学模式。多所高校开始探索将AI助教引入日常教学，用于个性化学习辅导、智能答疑、自动批改与编程实训等场景。",
                "专家指出，AI工具能够显著提升学习效率，但也对学术诚信、批判性思维培养提出了新挑战。如何在拥抱技术的同时引导学生合理使用，成为教育工作者关注的重点。"
            )
        ),
        SeedArticle(
            title = "国产开源操作系统迎来新版本，桌面生态持续完善",
            summary = "某国产开源操作系统发布最新长期支持版本，在内核性能、软件兼容性与桌面体验方面均有明显提升。",
            source = "科技频道", author = "技术编辑", category = "technology",
            publishTime = "2026-06-09", coverImageUrl = "$img/tech3/800/450",
            contentUrl = "https://example.com/news/os-release", isTop = false,
            content = listOf(
                "近日，国产开源操作系统发布最新长期支持（LTS）版本。新版本对系统内核进行了深度优化，启动速度与资源占用均有改善，并新增对大量国产硬件的原生支持。",
                "在桌面生态方面，新版本完善了应用商店，常用办公、开发与多媒体软件的兼容性进一步提高，为用户从其他平台迁移降低了门槛。"
            )
        ),
        SeedArticle(
            title = "校园无人配送车上线，快递取件实现“最后一百米”智能化",
            summary = "我校引入多台自动驾驶无人配送车，可将快递与外卖自动送至各宿舍楼下，学生扫码即可取件。",
            source = "校园新闻网", author = "后勤集团", category = "technology",
            publishTime = "2026-06-07", coverImageUrl = "$img/tech4/800/450",
            contentUrl = "https://example.edu.cn/news/delivery-robot", isTop = false,
            content = listOf(
                "近日，多台造型可爱的无人配送车开始在校园道路上穿梭。这些自动驾驶配送车能够自主识别路况、避让行人，将快递和外卖精准送达各宿舍楼下指定取货点。",
                "学生收到取件通知后，到指定地点扫描二维码即可打开舱门取件，整个过程不超过一分钟。后勤集团表示，无人配送有效缓解了高峰期取件拥堵，提升了校园物流效率。"
            )
        ),

        // ---------------- 体育 sports ----------------
        SeedArticle(
            title = "我校代表队在全国大学生田径锦标赛中斩获三金",
            summary = "在刚刚结束的全国大学生田径锦标赛上，我校代表队奋勇拼搏，共获得3枚金牌、2枚银牌，刷新两项校纪录。",
            source = "体育部", author = null, category = "sports",
            publishTime = "2026-06-12", coverImageUrl = "$img/sport1/800/450",
            contentUrl = "https://example.edu.cn/news/track-field", isTop = true,
            content = listOf(
                "在刚刚落幕的全国大学生田径锦标赛中，我校代表队表现出色，共摘得3枚金牌、2枚银牌和1枚铜牌，并刷新了男子4×100米接力和女子跳远两项校纪录。",
                "其中，男子接力队在决赛中顶住压力、密切配合，以微弱优势率先冲线，为学校赢得宝贵金牌。赛后队员表示，这份成绩离不开教练团队的科学训练和全队日复一日的刻苦付出。"
            )
        ),
        SeedArticle(
            title = "校园马拉松激情开跑，五千余名师生共赴青春之约",
            summary = "2026年校园马拉松日前鸣枪开跑，吸引五千余名师生参与，赛道串联起校园多个标志性景观。",
            source = "校园新闻网", author = "学生记者团", category = "sports",
            publishTime = "2026-06-10", coverImageUrl = "$img/sport2/800/450",
            contentUrl = "https://example.edu.cn/news/marathon", isTop = false,
            content = listOf(
                "伴随着清晨的第一缕阳光，2026年校园马拉松在主教学楼前鸣枪开跑。本次赛事设半程马拉松、欢乐跑等组别，吸引了五千余名师生踊跃参与。",
                "赛道精心设计，串联起图书馆、人工湖、樱花大道等多个校园标志性景观，跑者们在挥洒汗水的同时也尽享校园美景。组委会还在沿途设置补给站与加油助威点，营造出热烈的运动氛围。"
            )
        ),
        SeedArticle(
            title = "“院长杯”篮球联赛落幕，计算机学院夺冠",
            summary = "历时一个月的“院长杯”篮球联赛圆满落幕，计算机学院代表队在决赛中险胜，捧得冠军奖杯。",
            source = "校团委", author = null, category = "sports",
            publishTime = "2026-06-06", coverImageUrl = "$img/sport3/800/450",
            contentUrl = "https://example.edu.cn/news/basketball", isTop = false,
            content = listOf(
                "历时一个月、共40余场比赛的“院长杯”篮球联赛日前圆满落幕。在最终的冠军争夺战中，计算机学院代表队与机械学院展开激烈角逐，最终以两分优势险胜，捧起冠军奖杯。",
                "整个赛季，各院系球队拼搏进取、互相切磋，不仅展现了高超球技，也增进了院系间的友谊。现场观众座无虚席，呐喊助威声此起彼伏。"
            )
        ),

        // ---------------- 国际/财经 international ----------------
        SeedArticle(
            title = "多国高校代表来访，共商国际化人才培养合作",
            summary = "我校近日接待来自多个国家的高校代表团，双方围绕学生交换、联合培养与科研合作进行深入交流。",
            source = "国际合作处", author = null, category = "international",
            publishTime = "2026-06-11", coverImageUrl = "$img/intl1/800/450",
            contentUrl = "https://example.edu.cn/news/intl-cooperation", isTop = false,
            content = listOf(
                "近日，来自多个国家和地区的高校代表团到访我校，就深化国际化办学合作展开交流。双方围绕学生交换、学分互认、双学位联合培养以及联合科研等议题进行了深入探讨。",
                "国际合作处负责人表示，学校将持续拓展高质量国际合作伙伴，为学生提供更多海外交流学习机会，培养具有全球视野的高素质人才。"
            )
        ),
        SeedArticle(
            title = "全球科技产业观察：人工智能投资持续升温",
            summary = "据多家机构报告，全球人工智能相关领域投资持续增长，算力基础设施与行业应用成为资本关注焦点。",
            source = "财经参考", author = "财经编辑", category = "international",
            publishTime = "2026-06-09", coverImageUrl = "$img/intl2/800/450",
            contentUrl = "https://example.com/news/ai-investment", isTop = false,
            content = listOf(
                "据多家研究机构发布的最新报告显示，全球人工智能相关领域投资在过去一年持续保持高速增长。算力基础设施建设、行业垂直应用以及AI安全与治理，成为资本和产业共同关注的焦点。",
                "分析人士指出，随着大模型技术加速落地，AI正从概念走向规模化商用，但同时也面临能耗、数据隐私与监管合规等多重挑战。"
            )
        ),
        SeedArticle(
            title = "国际大学生程序设计竞赛区域赛收官，多支队伍晋级",
            summary = "国际大学生程序设计竞赛（ICPC）区域赛日前结束，来自全球的高校队伍同台竞技，多支队伍获得全球总决赛资格。",
            source = "赛事组委会", author = null, category = "international",
            publishTime = "2026-06-04", coverImageUrl = "$img/intl3/800/450",
            contentUrl = "https://example.com/news/icpc", isTop = false,
            content = listOf(
                "国际大学生程序设计竞赛（ICPC）区域赛日前圆满收官。来自全球各地高校的数百支队伍齐聚赛场，围绕算法与编程难题展开激烈角逐。",
                "经过数小时的紧张比拼，多支表现优异的队伍脱颖而出，获得了晋级全球总决赛的宝贵资格。本届赛事题目涵盖图论、动态规划、计算几何等多个方向，对选手的综合能力提出了很高要求。"
            )
        ),

        // ---------------- 推荐 recommend（综合热点） ----------------
        SeedArticle(
            title = "毕业季｜致即将启程的你：愿归来仍是少年",
            summary = "又是一年毕业季，校园里满是离别与祝福。本文记录下毕业生们的故事与寄语，愿每一位学子前程似锦。",
            source = "校园新闻网", author = "编辑部", category = "recommend",
            publishTime = "2026-06-14", coverImageUrl = "$img/rec1/800/450",
            contentUrl = "https://example.edu.cn/news/graduation", isTop = true,
            content = listOf(
                "六月的校园，凤凰花开，又到了一年一度的毕业季。林荫道上，身着学位服的同学们三三两两，定格下青春最美的瞬间。",
                "四年时光转瞬即逝，从初入校园的青涩懵懂，到如今即将奔赴各自的山海，每一位毕业生心中都装满了不舍与期待。有人选择继续深造，有人即将步入职场，也有人踏上创业之路。",
                "无论前路如何，母校永远是大家温暖的港湾。愿每一位毕业生都能带着这份记忆与勇气，在更广阔的天地间发光发热，归来仍是少年。"
            )
        ),
        SeedArticle(
            title = "校园食堂上新季｜十余款人气新菜品等你打卡",
            summary = "为提升师生用餐体验，各食堂联合推出十余款新菜品，从地方特色到健康轻食一应俱全，价格亲民。",
            source = "后勤集团", author = null, category = "recommend",
            publishTime = "2026-06-08", coverImageUrl = "$img/rec2/800/450",
            contentUrl = "https://example.edu.cn/news/canteen", isTop = false,
            content = listOf(
                "为不断提升师生用餐体验，本学期各食堂在广泛征集意见后联合推出十余款全新菜品，涵盖川湘、粤式、西北面食等地方特色，以及低脂轻食、营养简餐等健康选择。",
                "新菜品在保证口味的同时坚持平价惠民，多数定价与原有菜品持平。不少同学品尝后纷纷表示“食堂越来越好吃了”，并在校园社交平台上分享打卡。"
            )
        ),
        SeedArticle(
            title = "校园招聘指南｜简历、面试与offer选择，过来人这样说",
            summary = "求职季，如何打磨简历、从容应对面试、理性比较offer？多位优秀毕业生分享了他们的实用经验。",
            source = "就业指导中心", author = "就业指导中心", category = "recommend",
            publishTime = "2026-06-03", coverImageUrl = "$img/rec3/800/450",
            contentUrl = "https://example.edu.cn/news/career-guide", isTop = false,
            content = listOf(
                "又到一年求职季，面对简历投递、笔试面试、offer比较等一系列环节，不少同学感到无从下手。为此，就业指导中心邀请多位已成功签约的优秀毕业生分享经验。",
                "在简历方面，学长学姐们建议突出项目经历与量化成果，做到简洁清晰、一岗一投。面试环节则要提前了解企业与岗位，自信表达、真诚沟通。在offer选择上，应综合考虑发展平台、行业前景与个人兴趣，而非只看薪资。",
                "就业指导中心也提醒同学们，求职过程中要提高警惕，谨防各类招聘陷阱，遇到问题可随时向中心咨询求助。"
            )
        )
    )
}
