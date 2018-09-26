from django.urls import path
from . import views
app_name = "skill_tree"
urlpatterns = [
    path('', views.index, name="index"),
    path('<str:skill_family_name>/', views.skill_family, name="skill_family"),
    path(
        '<str:skill_family_name>/<str:skill_name>/', views.skill,
        name="skill"),
    path(
        '<str:skill_family_name>/<str:skill_name>/inc_method_times/<int:user_method_id>/',
        views.inc_method_times,
        name="inc_method_times"),
]
