from django.contrib import admin
from .models import Level, Method, MethodFamily, Skill, SkillFamily, User, UserMethod, UserSkill
# Register your models here.

admin.site.register(Level)
admin.site.register(Method)
admin.site.register(MethodFamily)
admin.site.register(Skill)
admin.site.register(SkillFamily)
admin.site.register(User)
admin.site.register(UserMethod)
admin.site.register(UserSkill)
