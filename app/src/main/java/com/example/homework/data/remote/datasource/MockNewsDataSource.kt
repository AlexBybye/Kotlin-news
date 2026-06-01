package com.example.homework.data.remote.datasource

import com.example.homework.data.remote.dto.NewsArticleDto
import com.example.homework.data.remote.dto.NewsDetailDto
import com.example.homework.data.remote.dto.NewsListResponseDto
import com.example.homework.data.remote.network.ResultWrapper
import com.example.homework.model.NewsCategory
import kotlinx.coroutines.delay

class MockNewsDataSource : NewsDataSource {

    override suspend fun getNews(category: NewsCategory): ResultWrapper<NewsListResponseDto> {
        delay(500)
        return ResultWrapper.Success(
            NewsListResponseDto(
                code = 0,
                message = "success",
                data = buildMockData(category)
            )
        )
    }

    override suspend fun getNewsDetail(newsId: String): ResultWrapper<NewsDetailDto> {
        delay(350)
        val detail = buildDetail(newsId)
            ?: return ResultWrapper.Error("未找到对应的新闻详情，请返回首页重试。")
        return ResultWrapper.Success(detail)
    }

    private fun buildMockData(category: NewsCategory): List<NewsArticleDto> {
        return when (category) {
            NewsCategory.RECOMMEND -> listOf(
                createNews(
                    id = "r1",
                    title = "校园科技节正式启动，多项创新作品集中亮相",
                    summary = "本周校园科技节在主会场正式开幕，学生团队展示了机器人、智能硬件与创意应用。",
                    source = "校园新闻",
                    category = category,
                    publishTime = "今天 09:30",
                    imageSeed = "recommend-1",
                    isTop = true
                ),
                createNews(
                    id = "r2",
                    title = "本周热点回顾：毕业季、竞赛周与社团招新同步展开",
                    summary = "校园活动密集展开，毕业生服务、创新竞赛与社团活动成为本周关注焦点。",
                    source = "综合频道",
                    category = category,
                    publishTime = "今天 08:20",
                    imageSeed = "recommend-2"
                ),
                createNews(
                    id = "r3",
                    title = "城市更新专题引发热议，青年视角成媒体关注重点",
                    summary = "多家媒体围绕青年参与城市治理和公共空间更新展开专题报道。",
                    source = "热点观察",
                    category = category,
                    publishTime = "昨天 20:10",
                    imageSeed = "recommend-3"
                )
            )

            NewsCategory.TECHNOLOGY -> listOf(
                createNews(
                    id = "t1",
                    title = "国产大模型应用持续落地，教育场景成重点方向",
                    summary = "AI 工具在教学辅助、内容生产和校园服务中的应用正在不断扩展。",
                    source = "科技日报",
                    category = category,
                    publishTime = "今天 10:00",
                    imageSeed = "technology-1",
                    isTop = true
                ),
                createNews(
                    id = "t2",
                    title = "新一代移动芯片发布，端侧 AI 性能进一步提升",
                    summary = "移动设备本地推理能力增强，为新闻推荐与多媒体处理带来更多可能。",
                    source = "数码前沿",
                    category = category,
                    publishTime = "今天 07:45",
                    imageSeed = "technology-2"
                ),
                createNews(
                    id = "t3",
                    title = "开源社区发布多项开发工具更新，效率提升明显",
                    summary = "新的构建工具和调试方案简化了 Android 与多端项目的协作流程。",
                    source = "开发者周刊",
                    category = category,
                    publishTime = "昨天 18:00",
                    imageSeed = "technology-3"
                )
            )

            NewsCategory.SPORTS -> listOf(
                createNews(
                    id = "s1",
                    title = "校运会田径项目进入决赛阶段，多个纪录被刷新",
                    summary = "短跑、跳远和接力比赛热度持续升温，现场观赛氛围热烈。",
                    source = "体育频道",
                    category = category,
                    publishTime = "今天 11:10",
                    imageSeed = "sports-1"
                ),
                createNews(
                    id = "s2",
                    title = "全国联赛迎来焦点战，年轻球员表现亮眼",
                    summary = "多位青年选手在关键比赛中打出高水平表现，成为赛事亮点。",
                    source = "赛事速报",
                    category = category,
                    publishTime = "今天 09:15",
                    imageSeed = "sports-2",
                    isTop = true
                ),
                createNews(
                    id = "s3",
                    title = "晨跑与夜骑成为校园健身新趋势",
                    summary = "越来越多学生加入校园运动打卡活动，健康生活方式持续升温。",
                    source = "校园体育",
                    category = category,
                    publishTime = "昨天 19:30",
                    imageSeed = "sports-3"
                )
            )

            NewsCategory.CAMPUS -> listOf(
                createNews(
                    id = "c1",
                    title = "图书馆延长开放时间，考试周服务同步升级",
                    summary = "为满足复习需求，图书馆新增夜间自习区域并优化借阅服务。",
                    source = "校园新闻",
                    category = category,
                    publishTime = "今天 12:00",
                    imageSeed = "campus-1",
                    isTop = true
                ),
                createNews(
                    id = "c2",
                    title = "志愿服务月启动，多个学院联合开展社区实践",
                    summary = "学生志愿者将围绕科普、环保和助老等主题开展系列公益活动。",
                    source = "学生工作处",
                    category = category,
                    publishTime = "今天 08:50",
                    imageSeed = "campus-2"
                ),
                createNews(
                    id = "c3",
                    title = "宿舍文化节开幕，创意空间改造作品集中展示",
                    summary = "各院系围绕宿舍文化建设和空间美化提交了多项优秀作品。",
                    source = "校园生活",
                    category = category,
                    publishTime = "昨天 17:20",
                    imageSeed = "campus-3"
                )
            )

            NewsCategory.INTERNATIONAL -> listOf(
                createNews(
                    id = "i1",
                    title = "多国青年创新论坛举行，绿色科技议题受关注",
                    summary = "论坛围绕能源转型、可持续校园与跨文化合作等方向展开交流。",
                    source = "国际新闻",
                    category = category,
                    publishTime = "今天 06:40",
                    imageSeed = "international-1"
                ),
                createNews(
                    id = "i2",
                    title = "海外高校合作项目扩容，交换学习申请通道开启",
                    summary = "新一轮国际合作项目增加了人工智能、传媒与设计方向名额。",
                    source = "国际交流中心",
                    category = category,
                    publishTime = "昨天 21:00",
                    imageSeed = "international-2",
                    isTop = true
                ),
                createNews(
                    id = "i3",
                    title = "全球数字媒体趋势报告发布，短视频与互动内容持续增长",
                    summary = "报告显示，年轻用户更偏好具有实时互动和社区属性的内容形式。",
                    source = "海外观察",
                    category = category,
                    publishTime = "昨天 15:10",
                    imageSeed = "international-3"
                )
            )
        }
    }

    private fun createNews(
        id: String,
        title: String,
        summary: String,
        source: String,
        category: NewsCategory,
        publishTime: String,
        imageSeed: String,
        isTop: Boolean = false
    ): NewsArticleDto {
        return NewsArticleDto(
            id = id,
            title = title,
            summary = summary,
            coverImageUrl = "https://picsum.photos/seed/$imageSeed/320/240",
            author = null,
            source = source,
            category = category.apiValue,
            publishTime = publishTime,
            contentUrl = "https://example.com/news/$id",
            isTop = isTop
        )
    }

    private fun buildDetail(newsId: String): NewsDetailDto? {
        return when (newsId) {
            "r1" -> createDetail(
                newsId = "r1",
                title = "校园科技节正式启动，多项创新作品集中亮相",
                summary = "本周校园科技节在主会场正式开幕，学生团队展示了机器人、智能硬件与创意应用。",
                source = "校园新闻",
                category = NewsCategory.RECOMMEND,
                publishTime = "今天 09:30",
                imageSeed = "recommend-1",
                content = listOf(
                    "校园科技节于今天上午正式开幕，主会场集中展示了来自多个学院的创新作品。参展项目覆盖机器人、智能硬件、校园服务和创意编程等方向，吸引了大量师生到场体验。",
                    "活动现场设置了路演、体验和互动问答等多个环节，不少学生团队围绕“技术服务校园生活”主题展示了完整方案，体现出跨学科合作的创新趋势。",
                    "学校表示，后续还将继续举办专题讲座、成果评选与创新训练营，希望通过连续活动提升学生的实践能力和技术表达能力。"
                ),
                relatedArticles = listOf("c1", "c2")
            )

            "r2" -> createDetail(
                newsId = "r2",
                title = "本周热点回顾：毕业季、竞赛周与社团招新同步展开",
                summary = "校园活动密集展开，毕业生服务、创新竞赛与社团活动成为本周关注焦点。",
                source = "综合频道",
                category = NewsCategory.RECOMMEND,
                publishTime = "今天 08:20",
                imageSeed = "recommend-2",
                content = listOf(
                    "本周校园热点集中在毕业服务、创新竞赛和社团招新三条主线上，不同年级学生都能在这一阶段找到与自己相关的活动内容。",
                    "毕业季相关服务包括简历咨询、就业指导和离校事务办理；创新竞赛周则以作品展示和项目答辩为核心，吸引了大量技术型团队参与。",
                    "与此同时，新一轮社团招新也同步开启，不少新生通过专题展位和体验活动了解校园文化，提升了整体参与度。"
                ),
                relatedArticles = listOf("c3", "s1")
            )

            "r3" -> createDetail(
                newsId = "r3",
                title = "城市更新专题引发热议，青年视角成媒体关注重点",
                summary = "多家媒体围绕青年参与城市治理和公共空间更新展开专题报道。",
                source = "热点观察",
                category = NewsCategory.RECOMMEND,
                publishTime = "昨天 20:10",
                imageSeed = "recommend-3",
                content = listOf(
                    "围绕城市更新的专题报道近日持续升温，青年群体在公共空间改造、社区治理和数字化服务中的参与度成为外界关注焦点。",
                    "报道指出，越来越多高校团队开始关注城市微更新项目，通过调研、设计和原型验证等方式，为社区提出更具操作性的改善方案。",
                    "相关专家认为，青年视角不仅能为城市更新带来更具活力的表达，也能促使公共项目更贴近实际生活场景。"
                ),
                relatedArticles = listOf("t1", "i3")
            )

            "t1" -> createDetail(
                newsId = "t1",
                title = "国产大模型应用持续落地，教育场景成重点方向",
                summary = "AI 工具在教学辅助、内容生产和校园服务中的应用正在不断扩展。",
                source = "科技日报",
                category = NewsCategory.TECHNOLOGY,
                publishTime = "今天 10:00",
                imageSeed = "technology-1",
                content = listOf(
                    "随着国产大模型能力持续增强，教育领域成为应用落地的重要方向之一。从课堂问答到作业辅助，再到校内信息服务，模型正在不断拓展使用场景。",
                    "多所高校已经开始尝试把大模型融入学习支持平台，通过总结资料、生成练习建议和智能检索等方式提升学生获取信息的效率。",
                    "业内人士表示，教育场景更看重内容准确性、可解释性与服务边界，未来相关产品仍需在安全性和稳定性上持续打磨。"
                ),
                relatedArticles = listOf("t2", "t3")
            )

            "t2" -> createDetail(
                newsId = "t2",
                title = "新一代移动芯片发布，端侧 AI 性能进一步提升",
                summary = "移动设备本地推理能力增强，为新闻推荐与多媒体处理带来更多可能。",
                source = "数码前沿",
                category = NewsCategory.TECHNOLOGY,
                publishTime = "今天 07:45",
                imageSeed = "technology-2",
                content = listOf(
                    "新一代移动芯片的发布进一步强化了端侧 AI 的推理能力，让更多复杂任务能够在本地设备中完成，减少对云端依赖。",
                    "在内容应用领域，本地模型推理有助于提升个性化推荐、图像处理和语音理解等场景的响应速度，也能增强隐私保护能力。",
                    "对于移动开发而言，这意味着未来应用在体验设计上可以融入更多即时、智能且离线可用的能力。"
                ),
                relatedArticles = listOf("t1", "i3")
            )

            "t3" -> createDetail(
                newsId = "t3",
                title = "开源社区发布多项开发工具更新，效率提升明显",
                summary = "新的构建工具和调试方案简化了 Android 与多端项目的协作流程。",
                source = "开发者周刊",
                category = NewsCategory.TECHNOLOGY,
                publishTime = "昨天 18:00",
                imageSeed = "technology-3",
                content = listOf(
                    "多项面向开发者的工具链更新近日发布，内容涉及构建加速、日志分析、依赖管理和跨端协作等多个方面。",
                    "不少团队反馈，更新后的工具在增量构建、问题定位和资源压缩等环节带来了较明显的效率提升，尤其适合中大型项目。",
                    "对于课程项目来说，合理使用这些工具有助于减少重复劳动，把更多精力投入到功能实现与架构设计中。"
                ),
                relatedArticles = listOf("t1", "r1")
            )

            "s1" -> createDetail(
                newsId = "s1",
                title = "校运会田径项目进入决赛阶段，多个纪录被刷新",
                summary = "短跑、跳远和接力比赛热度持续升温，现场观赛氛围热烈。",
                source = "体育频道",
                category = NewsCategory.SPORTS,
                publishTime = "今天 11:10",
                imageSeed = "sports-1",
                content = listOf(
                    "随着校运会田径项目进入决赛阶段，多个项目成绩刷新了近年校内纪录，赛场氛围持续高涨。",
                    "短跑、跳远和接力项目成为观众关注焦点，不少学院组织了集体观赛和加油活动，提升了赛事参与感。",
                    "组委会表示，后续还将发布最佳团队风采和体育精神奖项，以鼓励更多学生参与日常锻炼。"
                ),
                relatedArticles = listOf("s2", "s3")
            )

            "s2" -> createDetail(
                newsId = "s2",
                title = "全国联赛迎来焦点战，年轻球员表现亮眼",
                summary = "多位青年选手在关键比赛中打出高水平表现，成为赛事亮点。",
                source = "赛事速报",
                category = NewsCategory.SPORTS,
                publishTime = "今天 09:15",
                imageSeed = "sports-2",
                content = listOf(
                    "全国联赛最新一轮焦点战中，多位年轻球员在高压对抗中展现了成熟的比赛能力，成为外界关注重点。",
                    "赛后分析认为，青年球员在速度、执行力和临场应变上的表现尤为突出，这也给各级联赛的人才培养提供了更多信心。",
                    "不少体育媒体指出，年轻化阵容正在成为提升比赛观赏性和竞争力的重要方向。"
                ),
                relatedArticles = listOf("s1", "s3")
            )

            "s3" -> createDetail(
                newsId = "s3",
                title = "晨跑与夜骑成为校园健身新趋势",
                summary = "越来越多学生加入校园运动打卡活动，健康生活方式持续升温。",
                source = "校园体育",
                category = NewsCategory.SPORTS,
                publishTime = "昨天 19:30",
                imageSeed = "sports-3",
                content = listOf(
                    "晨跑和夜骑正在成为校园生活中的常见场景，越来越多学生通过打卡、社群活动和路线分享坚持锻炼。",
                    "这类运动方式门槛较低、参与灵活，既能帮助学生释放学习压力，也增强了校园中的运动氛围。",
                    "学校体育部门计划后续组织更多主题活动，引导学生建立长期、稳定的健康生活方式。"
                ),
                relatedArticles = listOf("s1", "c3")
            )

            "c1" -> createDetail(
                newsId = "c1",
                title = "图书馆延长开放时间，考试周服务同步升级",
                summary = "为满足复习需求，图书馆新增夜间自习区域并优化借阅服务。",
                source = "校园新闻",
                category = NewsCategory.CAMPUS,
                publishTime = "今天 12:00",
                imageSeed = "campus-1",
                content = listOf(
                    "为应对考试周高峰，图书馆宣布延长开放时间，并同步开放更多夜间自习区域，缓解座位紧张问题。",
                    "此外，借阅、咨询与资料检索等服务流程也进行了优化，学生可通过线上平台提前了解空闲区域和服务窗口安排。",
                    "不少同学表示，延时开放和更清晰的服务指引有效提升了复习体验，也体现了校园服务的细致化升级。"
                ),
                relatedArticles = listOf("c2", "c3")
            )

            "c2" -> createDetail(
                newsId = "c2",
                title = "志愿服务月启动，多个学院联合开展社区实践",
                summary = "学生志愿者将围绕科普、环保和助老等主题开展系列公益活动。",
                source = "学生工作处",
                category = NewsCategory.CAMPUS,
                publishTime = "今天 08:50",
                imageSeed = "campus-2",
                content = listOf(
                    "本学期志愿服务月正式启动，多个学院将围绕科普宣传、环保行动、社区陪伴和助老服务展开系列活动。",
                    "活动不仅强调服务时长，更关注项目质量与学生在真实场景中的组织、沟通和协作能力提升。",
                    "校方表示，后续将把部分优秀项目纳入长期实践计划，推动志愿服务从阶段性活动向常态化机制转变。"
                ),
                relatedArticles = listOf("c1", "c3")
            )

            "c3" -> createDetail(
                newsId = "c3",
                title = "宿舍文化节开幕，创意空间改造作品集中展示",
                summary = "各院系围绕宿舍文化建设和空间美化提交了多项优秀作品。",
                source = "校园生活",
                category = NewsCategory.CAMPUS,
                publishTime = "昨天 17:20",
                imageSeed = "campus-3",
                content = listOf(
                    "宿舍文化节近日开幕，多个院系围绕宿舍环境优化、创意收纳和集体文化建设展示了丰富成果。",
                    "部分作品结合环保材料、功能分区和主题设计，对有限空间进行了更高效的规划，获得现场较高关注。",
                    "活动主办方表示，希望通过文化节推动宿舍从单纯居住空间向更有温度的集体生活空间转变。"
                ),
                relatedArticles = listOf("c1", "s3")
            )

            "i1" -> createDetail(
                newsId = "i1",
                title = "多国青年创新论坛举行，绿色科技议题受关注",
                summary = "论坛围绕能源转型、可持续校园与跨文化合作等方向展开交流。",
                source = "国际新闻",
                category = NewsCategory.INTERNATIONAL,
                publishTime = "今天 06:40",
                imageSeed = "international-1",
                content = listOf(
                    "多国青年创新论坛近日举行，来自不同国家和地区的青年代表围绕绿色科技、校园可持续发展和国际合作展开深入交流。",
                    "论坛议题重点关注清洁能源、低碳生活方式以及青年如何通过技术和社群协作参与社会问题解决。",
                    "与会嘉宾普遍认为，跨文化合作将成为未来青年创新的重要能力，而校园正是培养这种能力的理想场景。"
                ),
                relatedArticles = listOf("i2", "i3")
            )

            "i2" -> createDetail(
                newsId = "i2",
                title = "海外高校合作项目扩容，交换学习申请通道开启",
                summary = "新一轮国际合作项目增加了人工智能、传媒与设计方向名额。",
                source = "国际交流中心",
                category = NewsCategory.INTERNATIONAL,
                publishTime = "昨天 21:00",
                imageSeed = "international-2",
                content = listOf(
                    "学校发布新一轮海外高校合作项目计划，交换学习与联合培养项目的专业覆盖面进一步扩大。",
                    "此次新增方向包括人工智能、传媒、设计等热门领域，为学生提供了更多国际交流与课程体验机会。",
                    "国际交流中心提醒，有意申请的同学需重点关注语言成绩、课程匹配度和申请时间节点，提前做好材料准备。"
                ),
                relatedArticles = listOf("i1", "i3")
            )

            "i3" -> createDetail(
                newsId = "i3",
                title = "全球数字媒体趋势报告发布，短视频与互动内容持续增长",
                summary = "报告显示，年轻用户更偏好具有实时互动和社区属性的内容形式。",
                source = "海外观察",
                category = NewsCategory.INTERNATIONAL,
                publishTime = "昨天 15:10",
                imageSeed = "international-3",
                content = listOf(
                    "最新发布的全球数字媒体趋势报告指出，短视频、直播互动和社区化内容继续保持增长，成为年轻用户的重要内容消费方式。",
                    "报告认为，未来媒体产品的竞争重点将从单向分发转向互动体验、内容参与感以及更灵活的内容组织方式。",
                    "这类趋势也为新闻产品设计提供了新的启发，即如何在信息效率与内容互动之间找到平衡。"
                ),
                relatedArticles = listOf("i1", "t2")
            )

            else -> null
        }
    }

    private fun createDetail(
        newsId: String,
        title: String,
        summary: String,
        source: String,
        category: NewsCategory,
        publishTime: String,
        imageSeed: String,
        content: List<String>,
        relatedArticles: List<String>
    ): NewsDetailDto {
        return NewsDetailDto(
            id = newsId,
            title = title,
            summary = summary,
            coverImageUrl = "https://picsum.photos/seed/$imageSeed/640/420",
            source = source,
            author = null,
            category = category.apiValue,
            publishTime = publishTime,
            content = content,
            contentUrl = "https://example.com/news/$newsId",
            relatedArticles = relatedArticles.mapNotNull(::buildRelatedArticle)
        )
    }

    private fun buildRelatedArticle(newsId: String): NewsArticleDto? {
        return buildAllNewsMap()[newsId]
    }

    private fun buildAllNewsMap(): Map<String, NewsArticleDto> {
        return NewsCategory.entries
            .flatMap(::buildMockData)
            .mapNotNull { article -> article.id?.let { id -> id to article } }
            .toMap()
    }
}
