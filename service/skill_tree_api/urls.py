from django.conf.urls import include, url
from rest_framework.documentation import include_docs_urls
from rest_framework.routers import DefaultRouter
from rest_framework.schemas import get_schema_view
from skill_tree_api import views

schema_view = get_schema_view(title='Pastebin API')

skill_family_detail = views.UserSkillFamilyViewSet.as_view({
    'get': 'all_skill'
})

router = DefaultRouter()
router.register(r'skill_family', views.UserSkillFamilyViewSet)
router.register(r'skills', views.SkillMethodViewSet)

urlpatterns = [
    url(r'^', include(router.urls)),
    url(r'^schema/$', schema_view),
    url(r'^docs/', include_docs_urls(title='Skill Tree API')),
    url(
        r'^skill_family/(?P<pk>[0-9]+)/all_skill/(?P<skill_family_id>[0-9]+)/$',
        skill_family_detail),
]
